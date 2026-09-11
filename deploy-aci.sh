#!/bin/bash
set -e

# ===================== CONFIGURAÇÕES =====================
RG="rg-clyvocare"
LOCATION="eastus"
ACR_NAME="acrclyvocare$RANDOM"      # nome do ACR precisa ser único globalmente
IMAGE_NAME="clyvocare-api"
IMAGE_TAG="v1"
ACI_NAME="aci-clyvocare"
DNS_LABEL="clyvocare-$RANDOM"       # também precisa ser único globalmente

# Senhas geradas na hora - nunca ficam salvas em nenhum arquivo do repo
DB_ADMIN_PASSWORD="Cly$(openssl rand -hex 6 | tr -d '\r\n')Aa1"
DB_APP_PASSWORD="App$(openssl rand -hex 6 | tr -d '\r\n')Bb2"
JWT_SECRET=$(openssl rand -base64 48 | tr -d '\r\n')
# ===========================================================

echo ">> 1) Criando o Resource Group..."
az group create --name "$RG" --location "$LOCATION"

echo ">> 2) Criando o Azure Container Registry (ACR)..."
az acr create --resource-group "$RG" --name "$ACR_NAME" --sku Basic --admin-enabled true

echo ">> 3) Buildando a imagem localmente com Docker..."
docker build -t "$IMAGE_NAME:$IMAGE_TAG" .

echo ">> 4) Obtendo credenciais do ACR e enviando a imagem..."
ACR_SERVER=$(az acr show --name "$ACR_NAME" --query loginServer -o tsv)
ACR_USER=$(az acr credential show --name "$ACR_NAME" --query username -o tsv)
ACR_PASS=$(az acr credential show --name "$ACR_NAME" --query "passwords[0].value" -o tsv)

echo "$ACR_PASS" | docker login "$ACR_SERVER" -u "$ACR_USER" --password-stdin
docker tag "$IMAGE_NAME:$IMAGE_TAG" "$ACR_SERVER/$IMAGE_NAME:$IMAGE_TAG"
docker push "$ACR_SERVER/$IMAGE_NAME:$IMAGE_TAG"

echo ">> 4b) Espelhando a imagem do Oracle para o ACR (evita rate limit do Docker Hub)..."
az acr import \
  --name "$ACR_NAME" \
  --source docker.io/gvenzl/oracle-xe:21-slim \
  --image oracle-xe:21-slim \
  --force

echo ">> 4c) Habilitando pull anonimo no ACR (evita bloqueio de autenticacao admin do ACI nesta assinatura)..."
az acr update --name "$ACR_NAME" --anonymous-pull-enabled true

echo ">> 5) Gerando o arquivo aci-clyvocare.yaml a partir do template (com senhas geradas na hora)..."
sed \
  -e "s#<ACR_SERVER>#$ACR_SERVER#g" \
  -e "s#<DNS_LABEL>#$DNS_LABEL#g" \
  -e "s#<DB_ADMIN_PASSWORD>#$DB_ADMIN_PASSWORD#g" \
  -e "s#<DB_APP_PASSWORD>#$DB_APP_PASSWORD#g" \
  -e "s#<JWT_SECRET>#$JWT_SECRET#g" \
  -e "s#gvenzl/oracle-xe:21-slim#$ACR_SERVER/oracle-xe:21-slim#g" \
  aci-clyvocare.template.yaml | sed '/imageRegistryCredentials:/,$d' > aci-clyvocare.yaml

# aci-clyvocare.yaml (com as senhas reais) fica só localmente - nunca commitar
if [ -f .gitignore ] && ! grep -q "^aci-clyvocare.yaml$" .gitignore; then
  echo "aci-clyvocare.yaml" >> .gitignore
elif [ ! -f .gitignore ]; then
  echo "aci-clyvocare.yaml" > .gitignore
fi

echo ">> 6) Criando o Container Group no ACI (App + Banco Oracle no mesmo grupo)..."
echo "   Aguardando 20s para o registro/imagens propagarem..."
sleep 20

MAX_RETRIES=3
for i in $(seq 1 $MAX_RETRIES); do
  if az container create --resource-group "$RG" --file aci-clyvocare.yaml; then
    echo "   Container Group criado com sucesso."
    break
  fi
  if [ "$i" -eq "$MAX_RETRIES" ]; then
    echo "   Falha ao criar o Container Group depois de $MAX_RETRIES tentativas."
    exit 1
  fi
  echo "   Tentativa $i falhou. Aguardando 20s antes de tentar de novo..."
  az container delete --resource-group "$RG" --name "$ACI_NAME" --yes >/dev/null 2>&1 || true
  sleep 20
done

echo ">> 7) Aguardando o Oracle inicializar (pode levar de 2 a 4 minutos)..."
sleep 150

echo ">> 8) Endereço público da aplicação:"
az container show \
  --resource-group "$RG" \
  --name "$ACI_NAME" \
  --query "{FQDN:ipAddress.fqdn, IP:ipAddress.ip, Status:instanceView.state}" \
  -o table

# Salva as credenciais geradas num arquivo local (nunca commitado) pra você usar
# no SQL Developer / Insomnia durante a gravação do vídeo
cat > deploy-secrets.txt <<EOF
Gerado em: $(date)
Oracle - usuario admin (system): senha = $DB_ADMIN_PASSWORD
Oracle - usuario da app (clyvocare): senha = $DB_APP_PASSWORD
JWT secret: $JWT_SECRET
EOF
if [ -f .gitignore ] && ! grep -q "^deploy-secrets.txt$" .gitignore; then
  echo "deploy-secrets.txt" >> .gitignore
elif [ ! -f .gitignore ]; then
  echo "deploy-secrets.txt" > .gitignore
fi

echo ""
echo "Swagger:  http://<IP_OU_FQDN_ACIMA>:8080/swagger-ui.html"
echo "Oracle:   <IP_OU_FQDN_ACIMA>:1521 / Service Name: XEPDB1 / usuario: clyvocare"
echo "Senha do usuario clyvocare (banco): veja o arquivo deploy-secrets.txt"
echo ""
echo "Para ver logs do app:    az container logs --resource-group $RG --name $ACI_NAME --container-name clyvocare-api"
echo "Para ver logs do Oracle: az container logs --resource-group $RG --name $ACI_NAME --container-name oracle-db"
echo "Para checar usuário não-root: az container exec --resource-group $RG --name $ACI_NAME --container-name clyvocare-api --exec-command whoami"
echo "Para apagar tudo no final: az group delete --name $RG --yes --no-wait"