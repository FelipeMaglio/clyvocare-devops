# ClyvoCare OS — API (Sprint 4: CI/CD com Azure DevOps)

Projeto da disciplina **DevOps Tools & Cloud Computing** (FIAP), Sprint 4. API REST em **Java + Spring Boot** para clínicas veterinárias, com pipeline de **CI/CD no Azure DevOps** que compila, testa, publica o artefato, gera a imagem Docker e faz o deploy no **Azure Container Instance (ACI)**, usando **Oracle em container na nuvem**.

## Equipe

| RM | Nome | Turma |
| -- | ---- | ----- |
| RM563512 | Felipe Maglio | <TURMA> |
| RM563509 | Vitória Valentina Maglio | <TURMA> |
| RM564930 | Mateus Granja dos Santos | <TURMA> |
| RM564339 | João Pedro Bitencourt Goldoni | <TURMA> |
| RM561786 | Marina Tamagnini Magalhães | <TURMA> |

## Links

- Repositório (GitHub): https://github.com/FelipeMaglio/clyvocare-devops
- Projeto no Azure DevOps: https://dev.azure.com/clyvocare-rm563512/Sprint%204%20%E2%80%93%20Azure%20DevOps
- Vídeo (YouTube): <LINK_DO_VIDEO>

## Descrição da solução

O **ClyvoCare OS** apoia a rotina de clínicas veterinárias com foco em prevenção e continuidade do cuidado. A API permite:

- cadastrar e gerenciar **pets** e seus **exames** (tabelas relacionadas, usadas no CRUD completo);
- registrar vacinas, consultas e tratamentos;
- gerar alertas (vacina atrasada, retorno pendente, tratamento abandonado), calcular risco do pet e recomendar cuidados por fase da vida;
- consultar o histórico clínico completo do pet e um dashboard consolidado.

### Benefícios para o negócio

- Menos vacinas e retornos esquecidos, com mais retorno dos tutores.
- Priorização de atendimentos pelo score de risco.
- Histórico clínico centralizado, sem perda de informação entre consultas.
- Entrega contínua: cada commit na `master` é testado e publicado automaticamente, reduzindo erro manual e tempo de deploy.

## Stack de tecnologias

| Camada | Tecnologia |
| ------ | ---------- |
| Aplicação | Java 25, Spring Boot, Spring Data JPA, Spring Security (JWT), Flyway |
| Build e testes | Gradle, JUnit |
| Banco de dados | Oracle XE 21 em container (Azure Container Instance), serviço `XEPDB1` |
| Containerização | Docker (Dockerfile na raiz) |
| Registro de imagens | Azure Container Registry (ACR) |
| Execução na nuvem | Azure Container Instance (ACI) |
| CI/CD | Azure DevOps (Repos, Pipelines YAML, Artifacts, Variable Group, Service Connection) |
| Agente de build | Agente self-hosted (pool `Default`) com Docker Desktop, Java 25 e Gradle |
| Documentação da API | Swagger / OpenAPI |

## Diagrama da arquitetura e fluxo CI/CD

![Arquitetura e fluxo CI/CD](docs/arquitetura-cicd.png)

### Fluxo numerado

| # | Etapa |
| - | ----- |
| 1 | O desenvolvedor faz `git push` na branch `master` (Azure Repos). O código também é espelhado no GitHub. |
| 2 | A mudança na `master` dispara o **CI** automaticamente. |
| 3 | O Azure Pipelines envia o job ao **agente self-hosted**. |
| 4 | Gradle compila o projeto e executa os **testes automáticos** (conectando no Oracle em nuvem). |
| 5 | Resultados dos testes e o **artefato `drop`** (.jar) são publicados no Azure DevOps. |
| 6 | `docker build` e `docker push` enviam a imagem ao **ACR**, com a tag do build. |
| 7 | O **CD** é disparado após o artefato gerado e recria o container da API no **ACI**. |
| 8 | O ACI puxa a imagem do ACR. |
| 9 | A API se conecta ao Oracle (JDBC, porta 1521). |
| 10 | O usuário final acessa a API e o Swagger pelo endereço público do ACI. |

## Pipeline (`azure-pipelines.yml`)

- **Trigger:** a cada alteração na branch `master`.
- **Pool:** agente self-hosted (`Default`).
- **Stage CI — Build e Testes**
  1. Gradle `clean build` (executa os testes automáticos).
  2. `PublishTestResults` publica o resultado dos testes.
  3. `CopyFiles` + `PublishBuildArtifacts` publicam o artefato `drop`.
  4. `AzureCLI` faz `docker build` e `docker push` no ACR.
- **Stage CD — Deploy no ACI** (depende do sucesso do CI)
  - Recria o container `aci-clyvocare-api` a partir de um arquivo YAML do ACI gerado em tempo de execução, usando a imagem da execução (`$(Build.BuildId)`).

### Variáveis protegidas

Usuário, senha e segredos **não** ficam no repositório. Ficam no **Variable Group `clyvocare-secrets`**, com valores marcados como secret (cadeado):

| Variável | Uso |
| -------- | --- |
| `DB_PASSWORD` | Senha do usuário do Oracle |
| `JWT_SECRET` | Segredo de assinatura do JWT |
| `ACR_PASSWORD` | Credencial do ACR |

Variáveis não sensíveis (`RG`, `ACR`, `IMAGE`, `DB_HOST`, `DB_USER`) ficam no próprio YAML.

## Banco de dados em nuvem

Oracle XE em container, executado no ACI (`aci-oracle`), com DNS público `oracle-clyvocare563512.eastus.azurecontainer.io`, porta `1521`, serviço `XEPDB1`. O esquema é criado e versionado pelo **Flyway** (`classpath:db/migration`) na inicialização da aplicação.

## Como reproduzir

### 1. Infraestrutura (Azure Cloud Shell, Bash)

```bash
RG=rg-clyvocare-s4
LOC=eastus
ACR=acrclyvocare563512

az group create -n $RG -l $LOC
az acr create -g $RG -n $ACR -l $LOC --sku Standard --admin-enabled true
az acr update -n $ACR --anonymous-pull-enabled true

read -s -p "Senha do banco (letras e numeros): " DB_PASSWORD; echo
az container create -g $RG -n aci-oracle -l $LOC \
  --image gvenzl/oracle-xe:21-slim-faststart \
  --os-type Linux --cpu 2 --memory 4 \
  --ports 1521 --ip-address Public \
  --dns-name-label oracle-clyvocare563512 \
  --secure-environment-variables ORACLE_PASSWORD=$DB_PASSWORD APP_USER_PASSWORD=$DB_PASSWORD \
  --environment-variables APP_USER=clyvocare

until az container logs -g $RG -n aci-oracle | grep -q "READY TO USE"; do sleep 20; done; echo "ORACLE PRONTO"
```

### 2. Azure DevOps

1. Criar o projeto **Sprint 4 – Azure DevOps** (Private, Git, processo Agile) e convidar o professor com acesso **Basic**.
2. Enviar o código para o Azure Repos na branch `master`:
   ```bash
   git remote add azure "<URL_DO_REPO_NO_AZURE_REPOS>"
   git push azure main:master
   ```
3. Criar o **Service Connection** `sc-azure` (Azure Resource Manager, grupo `rg-clyvocare-s4`).
4. Criar o **Variable Group** `clyvocare-secrets` com `DB_PASSWORD`, `JWT_SECRET` e `ACR_PASSWORD` como secret.
5. Registrar o **agente self-hosted** no pool `Default` (`config.cmd` e `run.cmd`), com Docker Desktop, Java 25 e Azure CLI instalados.
6. Criar a pipeline a partir do `azure-pipelines.yml` (Azure Repos Git → Existing YAML file → `master`).

### 3. Executar

Qualquer `git push azure main:master` dispara CI e CD. Após o deploy, a API fica em:

```
http://clyvocare-api-563512.eastus.azurecontainer.io:8080/swagger-ui/index.html
```

### 4. Testar o CRUD (pets e exames)

Pelo Swagger (ou por chamadas REST), executar inserir, consultar, atualizar e excluir em **pets** e **exames**, conferindo cada operação no banco:

```sql
SELECT * FROM pets;
SELECT * FROM exames;
```

(Conexão: host `oracle-clyvocare563512.eastus.azurecontainer.io`, porta `1521`, service name `XEPDB1`, usuário `clyvocare`.)

### 5. Desligar para não gastar crédito

```bash
az container stop -g rg-clyvocare-s4 -n aci-clyvocare-api
az container stop -g rg-clyvocare-s4 -n aci-oracle
```

## Estrutura do repositório

```
.
├── azure-pipelines.yml     # CI/CD
├── Dockerfile              # imagem da API
├── docker-compose.yml      # execução local
├── build.gradle / gradlew  # build
├── src/                    # código da aplicação
└── docs/
    ├── arquitetura-cicd.png
    └── arquitetura-cicd.svg
```
