# chmod +x deploy.sh
# sed -i 's/\r$//' deploy.sh
# ./deploy.sh

# ══════════════════════════════════════════════
# VARIÁVEIS
# ══════════════════════════════════════════════
GRUPO=clyvocare
LOCATION=southafricanorth
USER=azureuser
PASSWORD='Fiap@Cloud2026'
REPO_URL="https://github.com/FelipeMaglio/clyvocare-devops.git"

RG=rg-$GRUPO
VNET=vnet-$GRUPO
SUBNET=subnet-$GRUPO
NSG=nsg-$GRUPO
VM=vm-$GRUPO

# ══════════════════════════════════════════════
# 1. RESOURCE GROUP
# ══════════════════════════════════════════════
az group create \
  --name $RG \
  --location $LOCATION \
  --tags owner=$GRUPO environment=dev cost-center=fiap

# ══════════════════════════════════════════════
# 2. VNET E SUBNET
# ══════════════════════════════════════════════
az network vnet create \
  --resource-group $RG \
  --name $VNET \
  --address-prefix 10.10.0.0/16 \
  --subnet-name $SUBNET \
  --subnet-prefix 10.10.1.0/24 \
  --tags owner=$GRUPO environment=dev cost-center=fiap

# ══════════════════════════════════════════════
# 3. NSG
# ══════════════════════════════════════════════
az network nsg create \
  --resource-group $RG \
  --name $NSG \
  --tags owner=$GRUPO environment=dev cost-center=fiap

# ══════════════════════════════════════════════
# 4. REGRAS DO NSG (portas necessárias)
# ══════════════════════════════════════════════
az network nsg rule create \
  --resource-group $RG \
  --nsg-name $NSG \
  --name allow-ssh \
  --protocol Tcp \
  --priority 1000 \
  --destination-port-range 22 \
  --access Allow

az network nsg rule create \
  --resource-group $RG \
  --nsg-name $NSG \
  --name allow-http \
  --protocol Tcp \
  --priority 1001 \
  --destination-port-range 80 \
  --access Allow

az network nsg rule create \
  --resource-group $RG \
  --nsg-name $NSG \
  --name allow-8080 \
  --protocol Tcp \
  --priority 1002 \
  --destination-port-range 8080 \
  --access Allow

az network nsg rule create \
  --resource-group $RG \
  --nsg-name $NSG \
  --name allow-oracle \
  --protocol Tcp \
  --priority 1003 \
  --destination-port-range 1521 \
  --access Allow

# ══════════════════════════════════════════════
# 5. ASSOCIAR NSG À SUBNET
# ══════════════════════════════════════════════
az network vnet subnet update \
  --resource-group $RG \
  --vnet-name $VNET \
  --name $SUBNET \
  --network-security-group $NSG

# ══════════════════════════════════════════════
# 6. CRIAR VM LINUX
# Standard_D2s_v3 = 2 vCPUs, 8GB RAM
# Necessário para rodar Oracle XE + Spring Boot
# ══════════════════════════════════════════════
az vm create \
  --resource-group $RG \
  --name $VM \
  --image Ubuntu2204 \
  --admin-username $USER \
  --admin-password $PASSWORD \
  --authentication-type password \
  --size Standard_D2s_v3 \
  --vnet-name $VNET \
  --subnet $SUBNET \
  --nsg $NSG \
  --os-disk-size-gb 64 \
  --tags owner=$GRUPO environment=dev cost-center=fiap

# Captura o IP público da VM
VM_IP=$(az vm show --resource-group $RG --name $VM --show-details --query publicIps --output tsv)
echo "✅ VM criada! IP: $VM_IP"

# ══════════════════════════════════════════════
# 7. INSTALAR DOCKER, GIT, NANO E FERRAMENTAS
# ══════════════════════════════════════════════
az vm run-command invoke \
  --resource-group $RG \
  --name $VM \
  --command-id RunShellScript \
  --scripts '
    export DEBIAN_FRONTEND=noninteractive
    sudo apt-get update -y
    sudo apt-get install -y ca-certificates curl git nano vim htop

    sudo install -m 0755 -d /etc/apt/keyrings
    sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
    sudo chmod a+r /etc/apt/keyrings/docker.asc

    echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

    sudo apt-get update -y
    sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

    sudo systemctl enable docker
    sudo systemctl start docker

    # Adiciona azureuser ao grupo docker
    # Permite rodar docker SEM sudo — usuário sem privilégios administrativos
    sudo usermod -aG docker azureuser

    docker --version
    docker compose version
  '

# ══════════════════════════════════════════════
# 8. CLONAR REPOSITÓRIO E SUBIR CONTAINERS
# 2.1) App rodando em background (-d)
# 2.2) Usuário não-root (definido no Dockerfile)
# 2.3) Volume nomeado oracle-data (definido no docker-compose.yml)
# ══════════════════════════════════════════════
az vm run-command invoke \
  --resource-group $RG \
  --name $VM \
  --command-id RunShellScript \
  --scripts "
    cd /home/azureuser
    rm -rf clyvocare-devops
    git clone $REPO_URL clyvocare-devops
    cd clyvocare-devops

    # Sobe containers em background (-d = detached = background)
    sudo docker compose up -d --build

    echo 'Aguardando Oracle iniciar (3 minutos)...'
    sleep 180

    sudo docker compose ps
  "

# ══════════════════════════════════════════════
# RESUMO FINAL
# ══════════════════════════════════════════════
VM_IP=$(az vm show --resource-group $RG --name $VM --show-details --query publicIps --output tsv)

echo ""
echo "╔══════════════════════════════════════════════════════════╗"
echo "║  ✅ INFRAESTRUTURA E DEPLOY CONCLUÍDOS!                 ║"
echo "╠══════════════════════════════════════════════════════════╣"
echo "║  IP da VM  : $VM_IP                                     ║"
echo "║  API       : http://$VM_IP:8080                         ║"
echo "║  Swagger   : http://IP_DA_VM:8080/swagger-ui/index.html ║"
echo "╠══════════════════════════════════════════════════════════╣"
echo "║  ⚠️  DELETAR TUDO NO FINAL:                             ║"
echo "║  az group delete --name $RG --yes --no-wait             ║"
echo "╚══════════════════════════════════════════════════════════╝"