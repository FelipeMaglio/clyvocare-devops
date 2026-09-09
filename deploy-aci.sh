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
# ===========================================================

echo ">> 1) Criando o Resource Group..."
az group create --name "$RG" --location "$LOCATION"

echo ">> 2) Criando o Azure Container Registry (ACR)..."
az acr create --resource-group "$RG" --name "$ACR_NAME" --sku Basic --admin-enabled true

echo ">> 3) Buildando e enviando a imagem para o ACR (build feito na nuvem, sem precisar de Docker local)..."
az acr build --registry "$ACR_NAME" --image "$IMAGE_NAME:$IMAGE_TAG" .

echo ">> 4) Obtendo credenciais do ACR..."
ACR_SERVER=$(az acr show --name "$ACR_NAME" --query loginServer -o tsv)
ACR_USER=$(az acr credential show --name "$ACR_NAME" --query username -o tsv)
ACR_PASS=$(az acr credential show --name "$ACR_NAME" --query "passwords[0].value" -o tsv)

echo ">> 5) Gerando o arquivo aci-clyvocare.yaml a partir do template..."
sed \
  -e "s#<ACR_SERVER>#$ACR_SERVER#g" \
  -e "s#<ACR_USER>#$ACR_USER#g" \
  -e "s#<ACR_PASS>#$ACR_PASS#g" \
  -e "s#<DNS_LABEL>#$DNS_LABEL#g" \
  aci-clyvocare.template.yaml > aci-clyvocare.yaml

echo ">> 6) Criando o Container Group no ACI (App + Banco Oracle no mesmo grupo)..."
az container create --resource-group "$RG" --file aci-clyvocare.yaml

echo ">> 7) Aguardando o Oracle inicializar (pode levar de 2 a 4 minutos)..."
sleep 150

echo ">> 8) Endereço público da aplicação:"
az container show \
  --resource-group "$RG" \
  --name "$ACI_NAME" \
  --query "{FQDN:ipAddress.fqdn, IP:ipAddress.ip, Status:instanceView.state}" \
  -o table

echo ""
echo "Swagger:  http://<IP_OU_FQDN_ACIMA>:8080/swagger-ui.html"
echo "Oracle:   <IP_OU_FQDN_ACIMA>:1521 / Service Name: XEPDB1"
echo ""
echo "Para ver logs do app:    az container logs --resource-group $RG --name $ACI_NAME --container-name clyvocare-api"
echo "Para ver logs do Oracle: az container logs --resource-group $RG --name $ACI_NAME --container-name oracle-db"
echo "Para checar usuário não-root: az container exec --resource-group $RG --name $ACI_NAME --container-name clyvocare-api --exec-command whoami"
echo "Para apagar tudo no final: az group delete --name $RG --yes --no-wait"
