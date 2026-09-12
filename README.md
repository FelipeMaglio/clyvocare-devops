# 🐾 ClyvoCare OS — DevOps (ACR + ACI)

Entrega da **Sprint 3 — DevOps Tools & Cloud Computing** (FIAP), utilizando a API **ClyvoCare OS**, desenvolvida em Java + Spring Boot.

Plataforma inteligente de continuidade do cuidado veterinário, com monitoramento preventivo, alertas automáticos e histórico clínico longitudinal do pet.

---

# 👥 Equipe

| Nome | RM |
|---|---|
| Vitória Valentina Maglio | RM 563509 |
| Marina Tamagnini Magalhães | RM 561786 |
| Mateus Granja dos Santos | RM 564930 |
| Felipe Maglio Filho | RM 563512 |
| João Pedro Bitencourt | RM 564339 |

---

# 📑 Índice

- [📌 Descrição do Projeto](#-descrição-do-projeto)
- [💼 Benefícios para o Negócio](#-benefícios-para-o-negócio)
- [🛠️ Tecnologias Utilizadas](#️-tecnologias-utilizadas)
- [☁️ Arquitetura Cloud](#️-arquitetura-cloud)
- [🗄️ Banco de Dados](#️-banco-de-dados)
- [🚀 Como Instalar e Rodar Localmente](#-como-instalar-e-rodar-localmente)
- [☁️ Como Fazer o Deploy no Azure](#️-como-fazer-o-deploy-no-azure)
- [✅ Evidências de Funcionamento](#-evidências-de-funcionamento)
- [📌 Funcionalidades da API](#-funcionalidades-da-api)
- [📂 Estrutura do Repositório](#-estrutura-do-repositório)
- [🔗 Links](#-links)

---

# 📌 Descrição do Projeto

O **ClyvoCare OS** é uma API REST desenvolvida em **Java com Spring Boot** com o objetivo de promover a continuidade do cuidado veterinário, através de:

- monitoramento preventivo do histórico clínico do pet
- alertas automáticos de retorno e reforço de vacina
- histórico clínico longitudinal, centralizando consultas, exames, tratamentos, prescrições e vacinação

A plataforma cobre o ecossistema completo do cuidado veterinário, incluindo:

- cadastro e autenticação de tutores e veterinários (JWT)
- cadastro de pets vinculados a um tutor
- agendamento e conclusão de consultas, com geração automática de lembrete de retorno
- registro de exames e tratamentos vinculados ao pet e/ou à consulta
- prescrições vinculadas a um tratamento
- registro de vacinação com cálculo automático da data de reforço
- cadastro auxiliar de espécies, cidades, clínicas e itens de catálogo (vacinas/exames padronizados)

O código-fonte da aplicação é mantido no repositório [`ClyvoCareSC`](https://github.com/VitoriaMaglio/ClyvoCareSC); este repositório (`clyvocare-devops`) contém a containerização e a infraestrutura de nuvem que publicam essa API.

---

# 💼 Benefícios para o Negócio

| Benefício | Impacto |
|---|---|
| Lembrete automático de retorno | Reduz esquecimento de acompanhamento pós-consulta |
| Cálculo automático de reforço de vacina | Aumenta a adesão dos tutores ao calendário preventivo |
| Histórico clínico centralizado | Evita retrabalho e perda de informação entre clínicas |
| Controle de acesso por perfil (tutor x veterinário) | Protege dados sensíveis e restringe ações administrativas |
| Infraestrutura sob demanda (ACR + ACI) | Elimina o custo de manter uma VM ligada o tempo todo — os containers só rodam enquanto a solução está em uso |
| Banco Oracle em container | Dados persistidos com segurança e fácil recriação via Flyway |

---

# 🛠️ Tecnologias Utilizadas

- **Java 25**
- **Spring Boot 4.1**
- **Spring Data JPA / Hibernate** (modo `validate`)
- **Flyway** (versionamento de schema — migrations V1 a V14)
- **Spring Security + JWT**
- **Oracle Database** (container `gvenzl/oracle-xe`)
- **Swagger / OpenAPI** (springdoc 3.1.1)
- **Gradle**
- **Docker** (multi-stage, usuário não-root)
- **Microsoft Azure** (Container Registry + Container Instances)
- **Azure CLI**

---

# ☁️ Arquitetura Cloud

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

### Componentes utilizados

- Azure Container Registry (armazena a imagem da API)
- Azure Container Instances — Container Group com 2 containers (API + Oracle)
- Rede pública com FQDN/IP dinâmico, portas 8080 (API) e 1521 (Oracle)

Os dois containers ficam no **mesmo Container Group**, por isso se enxergam por `localhost`. Todos os recursos (Resource Group, ACR, ACI) são criados via **Azure CLI**, conforme exigido no enunciado — nenhum recurso é criado manualmente pelo Portal.

---

# 🗄️ Banco de Dados

O schema é controlado por **14 migrations do Flyway** (`src/main/resources/db/migration/V1__...sql` a `V14__...sql`), aplicadas automaticamente na subida da aplicação. O Hibernate roda em modo `validate` — nunca altera o schema, só confere que as entidades batem com o que o Flyway já criou.

O arquivo `script_bd.sql` na raiz deste repositório é a concatenação dessas 14 migrations e serve como o DDL completo exigido na entrega.

**Tabelas núcleo utilizadas no CRUD desta entrega:** `PETS` e `EXAMS`, relacionadas entre si por chave estrangeira (`exams.pet_id → pets.id`). O pet, por sua vez, é vinculado ao tutor por `pets.owner_id → owners.id`.

---

# 🚀 Como Instalar e Rodar Localmente

Pré-requisito: **Docker Desktop** instalado e rodando.

```bash
git clone https://github.com/FelipeMaglio/clyvocare-devops.git
cd clyvocare-devops
cp .env.example .env
```

Edite o `.env` com valores de senha/segredo à sua escolha, depois:

```bash
docker compose up --build
```

Aguarde o Oracle ficar `healthy` (2–4 minutos na primeira subida — acompanhe com `docker compose ps` em outro terminal). Depois:

- Swagger: http://localhost:8080/swagger-ui.html
- Health check: `curl http://localhost:8080/actuator/health`

Autenticação (JWT): cadastre um tutor, faça login em `/api/auth/login`, copie o token e use **Authorize** no Swagger com `Bearer <token>` para acessar as rotas protegidas.

Conexão direta no banco (SQL Developer): Host `localhost`, Porta `1521`, Service Name `XEPDB1`, Usuário `clyvocare`, Senha = a que você definiu no `.env`.

Para derrubar tudo:
```bash
docker compose down -v
```

---

# ☁️ Como Fazer o Deploy no Azure

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
4. Container Group no ACI com dois containers: `clyvocare-api` (porta 8080) e `oracle-db` (porta 1521), com senhas geradas aleatoriamente a cada execução

Ao final, ele imprime o IP/FQDN público da aplicação e grava um arquivo local `deploy-secrets.txt` (fora do controle de versão) com as senhas geradas, necessárias para conectar no banco.

### Limpar os recursos ao terminar

```bash
az group delete --name rg-clyvocare --yes --no-wait
```

---

# ✅ Evidências de Funcionamento

Após executar o `deploy-aci.sh`, use os comandos abaixo para validar que toda a infraestrutura foi criada corretamente.

## 🐳 Evidência 1 — Containers rodando

```bash
az container show --resource-group rg-clyvocare --name aci-clyvocare --query containers[].name -o table
```

### Resultado esperado

```text
Name
----------------
oracle-db
clyvocare-api
```

---

## 👤 Evidência 2 — Usuário não-root no container da API

```bash
az container exec --resource-group rg-clyvocare --name aci-clyvocare --container-name clyvocare-api --exec-command whoami
```

### Resultado esperado

```text
appuser
```

---

## 📜 Evidência 3 — Logs da aplicação funcionando

```bash
az container logs --resource-group rg-clyvocare --name aci-clyvocare --container-name clyvocare-api
```

### Resultado esperado

Deve aparecer algo semelhante a:

```text
Started ClyvoCareScApplication in X seconds
```

Confirmando que o Spring Boot e as migrations do Flyway subiram corretamente.

---

## 🗄️ Evidência 4 — Logs do banco Oracle

```bash
az container logs --resource-group rg-clyvocare --name aci-clyvocare --container-name oracle-db
```

### Resultado esperado

```text
DATABASE IS READY TO USE!
```

---

## 🗄️ Evidência 5 — Consulta direta no banco via SQL Developer

Dados de conexão (senha real gerada em `deploy-secrets.txt` a cada deploy):

| Campo | Valor |
|---|---|
| Hostname | IP/FQDN público exibido ao final do `deploy-aci.sh` |
| Port | 1521 |
| Service Name | XEPDB1 |
| Username | clyvocare |
| Password | ver `deploy-secrets.txt` |

Após conectar, execute:

```sql
SELECT * FROM owners;
SELECT * FROM pets;
SELECT * FROM exams;
```

### Estrutura esperada no painel

```text
ClyvoCare Azure
 └── Other Users
     └── CLYVOCARE
         └── Tables
             ├── OWNERS
             ├── PETS
             ├── EXAMS
             ├── APPOINTMENTS
             ├── VACCINATIONS
             ├── TREATMENTS
             ├── PRESCRIPTIONS
             ├── REMINDERS
             ├── VETERINARIANS
             ├── CLINICS
             ├── SPECIES
             ├── CITIES
             └── CATALOG_ITEMS
```

## ⚠️ Atenção sobre IP dinâmico

O IP/FQDN público muda a cada nova execução do `deploy-aci.sh`. Antes de conectar, verifique o valor exibido no resumo final do script e atualize o Hostname no SQL Developer (botão direito na conexão → Properties → Hostname).

---

# 📌 Funcionalidades da API

> ⚠️ A API utiliza autenticação JWT. Antes de usar os endpoints protegidos, realize o **registro e login** para obter o token, e cole em **Authorize** no Swagger como `Bearer {token}`.

## 🔐 Autenticação

```http
POST /api/auth/register/owner
POST /api/auth/register/veterinarian
POST /api/auth/login
```

Exemplo de body (registro de tutor):
```json
{
  "username": "tutor.exemplo",
  "password": "senha123",
  "name": "Nome do Tutor",
  "phone": "11999999999",
  "document": "12345678901"
}
```

## ✅ Tutores (Owners)

```http
GET /api/owners/me
PUT /api/owners/me
GET /api/owners/{id}
```

## ✅ Pets

```http
GET    /api/owners/{ownerId}/pets
GET    /api/pets/{id}
POST   /api/pets
PUT    /api/pets/{id}
DELETE /api/pets/{id}
```

Exemplo de body (POST/PUT):
```json
{
  "name": "Rex",
  "birthDate": "2022-01-15",
  "sex": "MALE",
  "currentWeight": 12.5,
  "breed": "Vira-lata",
  "ownerId": 1,
  "speciesId": 1
}
```

## ✅ Consultas (Appointments)

```http
GET   /api/pets/{petId}/appointments
GET   /api/appointments/{id}
POST  /api/appointments
PATCH /api/appointments/{id}/complete
PATCH /api/appointments/{id}/cancel
```

> Ao concluir uma consulta (`complete`), o sistema gera automaticamente um lembrete de retorno.

## ✅ Exames

```http
GET    /api/pets/{petId}/exams
GET    /api/exams/{id}
POST   /api/exams
PUT    /api/exams/{id}
DELETE /api/exams/{id}
```

Exemplo de body (POST/PUT) — requer perfil **veterinário**:
```json
{
  "examType": "Hemograma completo",
  "requestDate": "2026-09-11",
  "petId": 1
}
```

## ✅ Tratamentos e Prescrições

```http
GET   /api/pets/{petId}/treatments
POST  /api/treatments
PUT   /api/treatments/{id}
PATCH /api/treatments/{id}/complete
PATCH /api/treatments/{id}/suspend

GET  /api/treatments/{treatmentId}/prescriptions
POST /api/prescriptions
```

## ✅ Vacinação

```http
GET  /api/pets/{petId}/vaccinations
POST /api/vaccinations
```

Exemplo de body (POST):
```json
{
  "applicationDate": "2026-09-11",
  "petId": 1,
  "catalogItemId": 1
}
```

> Ao registrar a vacina, o sistema **calcula automaticamente a data do próximo reforço**.

## ✅ Lembretes (Reminders)

```http
GET   /api/owners/me/reminders
PATCH /api/reminders/{id}/sent
PATCH /api/reminders/{id}/confirmed
```

## ✅ Cadastros Auxiliares

```http
GET/POST/PUT/DELETE  /api/species
GET/POST/PUT/DELETE  /api/cities
GET/POST/PUT/DELETE  /api/clinics
GET/POST/PUT/DELETE  /api/catalog-items
GET                  /api/veterinarians
GET/PUT              /api/veterinarians/me
```

---

# 📂 Estrutura do Repositório

```text
clyvocare-devops/
├── src/                          # código-fonte da API (Java 25 / Spring Boot 4.1)
├── build.gradle, settings.gradle, gradlew   # build Gradle
├── Dockerfile                    # build multi-stage, usuário não-root
├── docker-compose.yml            # ambiente local (app + Oracle)
├── .env.example                  # modelo de variáveis de ambiente (sem segredos reais)
├── aci-clyvocare.template.yaml   # definição do Container Group (ACI)
├── deploy-aci.sh                 # cria RG + ACR + ACI via Azure CLI
├── script_bd.sql                 # DDL consolidado (migrations do Flyway)
└── README.md
```

---

# 🔗 Links

- Repositório do código-fonte: https://github.com/VitoriaMaglio/ClyvoCareSC
- Link do vídeo: https://youtu.be/dW93LktXAXY
