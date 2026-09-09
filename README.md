# 🐾 ClyvoCare OS — DevOps (ACR + ACI)

Plataforma inteligente de continuidade do cuidado veterinário — entrega da Sprint 3 de **DevOps Tools & Cloud Computing** (FIAP).

## 📌 Descrição da Solução

O ClyvoCare OS é uma API REST em Java (Spring Boot) que promove a continuidade do cuidado veterinário através de monitoramento preventivo, alertas automáticos e histórico clínico longitudinal do pet. A API cobre cadastro de tutores, veterinários, pets, consultas, exames, tratamentos, prescrições e vacinação, com dois fluxos automáticos: geração de lembrete de retorno ao concluir uma consulta, e cálculo automático da data de reforço ao registrar uma vacina.

O código-fonte da aplicação é mantido no repositório [`ClyvoCareSC`](https://github.com/VitoriaMaglio/ClyvoCareSC); este repositório (`clyvocare-devops`) contém a containerização e a infraestrutura de nuvem que publicam essa API.

## 💡 Benefícios para o Negócio

- Reduz esquecimento de retorno e reforço de vacina, aumentando a adesão dos tutores ao cuidado preventivo do pet.
- Centraliza o histórico clínico completo (consultas, exames, tratamentos, vacinas) num único lugar, evitando retrabalho entre clínicas.
- Controle de acesso por perfil (tutor x profissional de saúde) protege dados sensíveis e restringe ações administrativas.
- Infraestrutura sob demanda (ACR + ACI) elimina o custo de manter uma VM ligada o tempo todo — os containers só rodam enquanto a solução está em uso.

## 🏗️ Arquitetura Cloud

Solução **totalmente containerizada** (Opção 1 do enunciado: **ACR + ACI**), sem VM:

```
                         ┌─────────────────────────────┐
   docker build/push     │   Azure Container Registry   │
   ────────────────────► │   (armazena a imagem da API) │
                         └──────────────┬───────────────┘
                                        │ pull da imagem
                                        ▼
                         ┌─────────────────────────────────────┐
                         │   Azure Container Instances (ACI)     │
                         │   Container Group "aci-clyvocare"     │
                         │  ┌───────────────┐ ┌────────────────┐│
                         │  │ clyvocare-api │ │   oracle-db     ││
                         │  │  (porta 8080) │◄►│  (porta 1521)  ││
                         │  │  usuário não  │ │ gvenzl/oracle-  ││
                         │  │  -root        │ │ xe:21-slim      ││
                         │  └───────────────┘ └────────────────┘│
                         └─────────────────┬─────────────────────┘
                                           │ IP/FQDN público
                                           ▼
                                 Usuário / SQL Developer
```

Os dois containers ficam no **mesmo Container Group**, por isso se enxergam por `localhost` (igual acontecia entre serviços no Docker Compose local). Todos os recursos (Resource Group, ACR, ACI) são criados via **Azure CLI**, conforme exigido no enunciado.

## 🛠️ Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 25 |
| Framework | Spring Boot 4.1 |
| Build | Gradle |
| Persistência | Spring Data JPA / Hibernate (modo `validate`) |
| Versionamento de schema | Flyway (migrations V1–V14) |
| Autenticação | Spring Security + JWT |
| Banco de dados | Oracle Database (container `gvenzl/oracle-xe`) |
| Containerização | Docker (multi-stage, usuário não-root) |
| Cloud | Azure Container Registry + Azure Container Instances |

## 📂 Estrutura deste repositório

```
clyvocare-devops/
├── src/                          # código-fonte sincronizado de ClyvoCareSC
├── build.gradle, settings.gradle, gradlew   # build Gradle
├── Dockerfile                    # build multi-stage, usuário não-root
├── docker-compose.yml            # ambiente local (app + Oracle)
├── aci-clyvocare.template.yaml   # definição do Container Group (ACI)
├── deploy-aci.sh                 # cria RG + ACR + ACI via Azure CLI
├── sync-new-source.sh            # traz o código novo de ClyvoCareSC (somente leitura)
├── script_bd.sql                 # DDL consolidado (gerado das migrations do Flyway)
└── README.md
```

## 🗄️ Banco de Dados

O schema é controlado por **14 migrations do Flyway** (`src/main/resources/db/migration/V1__...sql` a `V14__...sql`), aplicadas automaticamente na subida da aplicação. O Hibernate roda em modo `validate` — nunca altera o schema, só confere que as entidades batem com o que o Flyway já criou.

O arquivo `script_bd.sql` na raiz deste repositório é a concatenação dessas 14 migrations (gerado pelo `sync-new-source.sh`) e serve como o DDL completo exigido na entrega.

Tabelas núcleo usadas no CRUD desta entrega: `PETS` e `APPOINTMENTS` (consultas), relacionadas por chave estrangeira ao tutor (`OWNERS`) e ao pet.

## 🚀 Como Rodar Localmente

Pré-requisito: **Docker Desktop** instalado e rodando.

```bash
git clone https://github.com/FelipeMaglio/clyvocare-devops.git
cd clyvocare-devops
docker compose up --build
```

Aguarde o Oracle ficar `healthy` (2–4 minutos na primeira subida — acompanhe com `docker compose ps` em outro terminal). Depois:

- Swagger: http://localhost:8080/swagger-ui.html
- Health check: `curl http://localhost:8080/actuator/health`

Autenticação (JWT): cadastre um tutor, faça login em `/api/auth/login`, copie o token e use **Authorize** no Swagger com `Bearer <token>` para acessar as rotas protegidas.

Conexão direta no banco (SQL Developer): Host `localhost`, Porta `1521`, Service Name `XEPDB1`, Usuário `clyvocare`, Senha `Clyvo@2026`.

Para derrubar tudo:
```bash
docker compose down -v
```

## ☁️ Como Fazer o Deploy no Azure

Pré-requisito: acesso ao [Azure Cloud Shell](https://shell.azure.com) (ou Azure CLI logado localmente).

```bash
git clone https://github.com/FelipeMaglio/clyvocare-devops.git
cd clyvocare-devops
chmod +x deploy-aci.sh
./deploy-aci.sh
```

O script cria, **tudo via Azure CLI**:
1. Resource Group
2. Azure Container Registry (ACR)
3. Build da imagem da API direto na nuvem (`az acr build`)
4. Container Group no ACI com dois containers: `clyvocare-api` (porta 8080) e `oracle-db` (porta 1521)

Ao final, ele imprime o IP/FQDN público da aplicação.

### Evidências para o vídeo

```bash
# usuário não-root do container do app
az container exec --resource-group rg-clyvocare --name aci-clyvocare --container-name clyvocare-api --exec-command whoami

# logs
az container logs --resource-group rg-clyvocare --name aci-clyvocare --container-name clyvocare-api
az container logs --resource-group rg-clyvocare --name aci-clyvocare --container-name oracle-db
```

Faça o CRUD completo (inserir, alterar, excluir, consultar) pela API e confirme cada operação com `SELECT` direto no Oracle via SQL Developer, usando o IP/FQDN público na porta 1521.

### Limpar os recursos ao terminar

```bash
az group delete --name rg-clyvocare --yes --no-wait
```

## 🔄 Atualizando o código-fonte a partir do ClyvoCareSC

Este repositório **não modifica** o repositório original `ClyvoCareSC` — apenas lê dele. Para sincronizar uma versão mais nova do código:

```bash
chmod +x sync-new-source.sh
./sync-new-source.sh
git add -A
git commit -m "chore: sincroniza codigo com ClyvoCareSC"
git push
```

## 👥 Integrantes

| Nome | RM |
|---|---|
| Vitória Valentina Maglio | RM 563509 |
| Marina Tamagnini Magalhães | RM 561786 |
| Mateus Granja dos Santos | RM 564930 |
| Felipe Maglio Filho | RM 563512 |
| João Pedro Bitencourt | RM 564339 |

## 🔗 Links

- Repositório do código-fonte: https://github.com/VitoriaMaglio/ClyvoCareSC
- Link do vídeo: _adicionar antes da entrega_
