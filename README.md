# 🐾 ClyvoCare OS — API

Projeto desenvolvido para o **Challenge 2026** utilizando **Java + Spring Boot**.

Sistema inteligente para clínicas veterinárias gerenciarem pets, vacinas, consultas, tratamentos, alertas e histórico clínico.

---

# 👥 Equipe

| Nome | RM |
|---|---|
| Felipe Maglio | RM563512 |
| Vitória Valentina Maglio | RM563509 |
| Mateus Granja dos Santos | RM564930 |
| João Pedro Bitencourt Goldoni | RM564339 |
| Marina Tamagnini Magalhães | RM561786 |

---

# 📑 Índice

- [📌 Descrição do Projeto](#-descrição-do-projeto)
- [🎯 Objetivo da API](#-objetivo-da-api)
- [💼 Benefícios para o Negócio](#-benefícios-para-o-negócio)
- [🛠️ Tecnologias Utilizadas](#️-tecnologias-utilizadas)
- [☁️ Arquitetura Cloud](#️-arquitetura-cloud)
- [🚀 Como Instalar e Rodar (How To)](#-como-instalar-e-rodar-how-to)
- [📜 Script Azure CLI](#-script-azure-cli)
- [🐳 Dockerfile](#-dockerfile)
- [🐳 Docker Compose](#-docker-compose)
- [📌 Funcionalidades da API](#-funcionalidades-da-api)
- [⭐ Diferenciais da API](#-diferenciais-da-api)
- [📂 Estrutura do Projeto](#-estrutura-do-projeto)

---

# 📌 Descrição do Projeto

O **ClyvoCare OS** é uma API REST desenvolvida em **Java com Spring Boot** com o objetivo de promover a continuidade do cuidado veterinário através de:

- monitoramento preventivo
- alertas inteligentes
- histórico longitudinal do pet

A plataforma organiza de forma completa o ecossistema de clínicas veterinárias, incluindo:

- cadastro e gestão de pets com perfil por espécie
- fase de vida automática calculada por espécie *(Filhote / Adulto / Idoso)*
- lembretes inteligentes baseados na fase da vida
- histórico clínico completo
- controle de vacinas
- consultas e tratamentos
- dashboard clínico com dados consolidados

A solução foi criada com base no desafio proposto pela **CLYVO VET — Infraestrutura do futuro da medicina veterinária digital**, focando em:

- prevenção veterinária
- engajamento contínuo do tutor
- redução do abandono de tratamentos
- acompanhamento clínico inteligente
- centralização do histórico do pet

---

# 🎯 Objetivo da API

O sistema possui funcionalidades inteligentes capazes de:

- gerar alertas preventivos
- calcular risco de saúde do pet
- recomendar cuidados por fase da vida
- monitorar vacinas atrasadas
- acompanhar tratamentos ativos
- fornecer histórico longitudinal completo

---

# 💼 Benefícios para o Negócio

| Benefício | Impacto |
|---|---|
| Alertas automáticos de vacinas atrasadas | Reduz falhas no calendário vacinal e aumenta retorno dos tutores |
| Score de risco do pet | Permite priorização de atendimentos críticos pela clínica |
| Recomendações por fase da vida | Aumenta engajamento e fidelização do tutor |
| Histórico longitudinal completo | Elimina perda de informações clínicas entre consultas |
| Dashboard consolidado | Dá visibilidade gerencial para a clínica tomar decisões |
| Infraestrutura em nuvem Azure | Alta disponibilidade e escalabilidade sem servidor físico |
| Banco Oracle em container | Dados persistidos com segurança e fácil restauração |

---

# 🛠️ Tecnologias Utilizadas

- **Java 21**
- **Spring Boot**
- **Spring Data JPA**
- **Spring Validation**
- **Spring Cache**
- **Oracle Database**
- **Swagger / OpenAPI**
- **Maven**
- **Lombok**
- **Docker**
- **Docker Compose**
- **Microsoft Azure**
- **Azure CLI**

---

# ☁️ Arquitetura Cloud

A infraestrutura do projeto foi construída utilizando recursos da **Microsoft Azure**, garantindo:

- escalabilidade
- disponibilidade
- persistência de dados
- automação de deploy
- facilidade de gerenciamento

### Componentes utilizados

- Azure Virtual Machine (Ubuntu)
- Oracle Database em container Docker
- API Spring Boot containerizada
- Azure Network Security Group (NSG)
- Docker Compose para orquestração

---

# 🚀 Como Instalar e Rodar (How To)

## ✅ Pré-requisitos

- Conta Microsoft Azure com créditos ativos
- Acesso ao Azure Cloud Shell

Acesse:

```bash
https://shell.azure.com
```

---

## ☁️ Passo 1 — Acesse o Azure Cloud Shell

Acesse:

```bash
https://shell.azure.com
```

Faça login com sua conta Azure e selecione **Bash** quando solicitado.

---

## 📤 Passo 2 — Faça upload do script

O arquivo `deploy.sh` está disponível dentro do repositório:

```bash
https://github.com/FelipeMaglio/clyvocare-devops.git
```

No Azure Cloud Shell:

1. Clique em:

```text
Manage Files → Upload
```

2. Faça upload do arquivo:

```text
deploy.sh
```

3. Execute os comandos:

```bash
chmod +x deploy.sh

sed -i 's/\r$//' deploy.sh

./deploy.sh
```

---

## ⏳ Passo 3 — Aguarde o deploy

O processo leva aproximadamente:

```text
15 minutos
```

---

# ✅ Evidências do funcionamento da infraestrutura

Após executar o `deploy.sh`, rode os comandos abaixo para validar que toda a infraestrutura foi criada corretamente.

---

## 🐳 Evidência 1 — Containers rodando em background

```bash
az vm run-command invoke \
--resource-group rg-clyvocare \
--name vm-clyvocare \
--command-id RunShellScript \
--scripts 'cd /home/azureuser/clyvocare-devops && sudo docker compose ps'
```

### Resultado esperado

Os dois containers devem aparecer com status:

```text
Up
```

---

## 👤 Evidência 2 — Usuário não-root

```bash
az vm run-command invoke \
--resource-group rg-clyvocare \
--name vm-clyvocare \
--command-id RunShellScript \
--scripts 'sudo docker exec clyvocare-api whoami'
```

### Resultado esperado

```text
appuser
```

---

## 💾 Evidência 3 — Volume nomeado

```bash
az vm run-command invoke \
--resource-group rg-clyvocare \
--name vm-clyvocare \
--command-id RunShellScript \
--scripts 'sudo docker volume ls'
```

### Resultado esperado

```text
clyvocare-devops_oracle-data
```

---

## 📜 Evidência 4 — Logs da aplicação funcionando

```bash
az vm run-command invoke \
--resource-group rg-clyvocare \
--name vm-clyvocare \
--command-id RunShellScript \
--scripts 'cd /home/azureuser/clyvocare-devops && sudo docker compose logs --tail=20 app'
```

### Resultado esperado

Deve aparecer algo semelhante a:

```text
Started Application in X seconds
```

Confirmando que o Spring Boot iniciou corretamente.

---

Após finalizar, acesse:

### Swagger

```bash
http://IP_DA_VM:8080/swagger-ui.html
```

### API

```bash
http://IP_DA_VM:8080
```

---

# 🗄️ Passo 3.1 — Conectar no banco via SQL Developer (Opcional)

Após o deploy, é possível conectar diretamente no banco Oracle utilizando o **SQL Developer**.

### Download do SQL Developer

```bash
https://www.oracle.com/tools/downloads/sqldev-downloads.html
```

---

## 🔗 Dados de conexão

| Campo | Valor |
|---|---|
| Name | ClyvoCare Azure |
| Username | clyvocare |
| Password | Clyvo@2026 |
| Connection Type | Basic |
| Hostname | IP_DA_VM |
| Port | 1521 |
| Service Name | XEPDB1 |

⚠️ **IMPORTANTE:**  
Marcar **Service Name** e NÃO **SID**.

---

## 📋 Passo a passo

### 1️⃣ Abrir o SQL Developer

Abra o aplicativo normalmente.

---

### 2️⃣ Criar nova conexão

Clique em:

```text
Connections → New Connection
```

ou no botão:

```text
+
```

verde no canto esquerdo.

---

### 3️⃣ Preencher os dados

Utilize os dados informados anteriormente.

---

### 4️⃣ Testar conexão

Clique em:

```text
Test
```

O resultado esperado:

```text
Status: Success
```

---

### 5️⃣ Conectar

Clique em:

```text
Connect
```

---

## 📂 Estrutura esperada no painel

Após conectar, expanda:

```text
ClyvoCare Azure
 └── Other Users
     └── CLYVOCARE
         └── Tables
             ├── TUTORES
             ├── PETS
             ├── VACINAS
             ├── CONSULTAS
             └── TRATAMENTOS
```

---

## 🔎 Consultar dados via SQL

Abra um SQL Worksheet e execute:

```sql
SELECT * FROM tutores;

SELECT * FROM pets;

SELECT * FROM vacinas;

SELECT * FROM consultas;

SELECT * FROM tratamentos;
```

---

## ⚠️ Atenção sobre IP dinâmico

O IP da VM muda sempre que o deploy é executado novamente.

Antes de conectar:

1. Verifique o IP exibido no resumo final do script
2. Atualize o Hostname no SQL Developer

Caminho:

```text
Botão direito na conexão → Properties
```

Depois atualize o campo:

```text
Hostname
```

---

# 🔐 Passo 4 — Conectar na VM via SSH (Opcional)

```bash
ssh azureuser@IP_DA_VM
```

Senha:

```text
Fiap@Cloud2026
```

---

# 🛠️ Comandos úteis dentro da VM

## 📦 Ver status dos containers

```bash
docker compose ps
```

---

## 📜 Ver logs da aplicação

```bash
docker compose logs -f app
```

---

## 🗄️ Ver logs do Oracle

```bash
docker compose logs -f oracle
```

---

## 👤 Verificar usuário não-root

```bash
docker exec clyvocare-api whoami
```

---

## 💾 Ver volumes Docker

```bash
docker volume ls
```

# 🗑️ Passo 5 — Deletar tudo no final

```bash
az group delete --name rg-clyvocare --yes --no-wait
```

---

## ✅ Verificar se os recursos foram removidos

Após alguns minutos, execute:

```bash
az group list --output table
```

O Resource Group:

```text
rg-clyvocare
```

não deve mais aparecer na lista.

---

# 📜 Script Azure CLI

O projeto utiliza automação via **Azure CLI** para:

- criação da infraestrutura
- configuração da VM
- instalação do Docker
- deploy da API
- deploy do Oracle Database

---

# 🐳 Dockerfile

A API é executada em container Docker para garantir:

- padronização do ambiente
- facilidade de deploy
- portabilidade
- escalabilidade

---

# 🐳 Docker Compose

O Docker Compose é utilizado para orquestrar:

- API Spring Boot
- Banco Oracle
- Redes Docker
- Volumes persistentes

---

# 📌 Funcionalidades da API

# ✅ Cadastro de Tutores

### Permite

- cadastrar tutor
- listar tutores
- buscar tutor por ID
- atualizar tutor
- deletar tutor

### Endpoints

```http
POST /tutores
GET /tutores
GET /tutores/{id}
PUT /tutores/{id}
DELETE /tutores/{id}
```

---

# ✅ Cadastro de Pets

### Permite

- cadastrar pets
- vincular pets aos tutores
- listar pets
- buscar pets por espécie
- buscar pets por risco
- atualizar dados do pet

### Endpoints

```http
POST /pets
GET /pets
GET /pets/{id}
PUT /pets/{id}
DELETE /pets/{id}
GET /pets/especie?nome=cachorro
GET /pets/risco?nivel=alto
```

---

# ✅ Controle de Vacinas

### Permite

- registrar vacina
- verificar vacinas atrasadas
- listar histórico vacinal

### Endpoints

```http
POST /vacinas
GET /vacinas
GET /vacinas/atrasadas
GET /vacinas/pet/{id}
```

---

# ✅ Controle de Consultas

### Permite

- registrar consultas
- registrar observações clínicas
- acompanhar retornos pendentes

### Endpoints

```http
POST /consultas
GET /consultas
GET /consultas/pet/{id}
GET /consultas/retornos
```

---

# ✅ Controle de Tratamentos

### Permite

- registrar medicamentos
- controlar tratamentos ativos
- identificar abandono de tratamento

### Endpoints

```http
POST /tratamentos
GET /tratamentos
GET /tratamentos/ativos
GET /tratamentos/pet/{id}
```

---

# ⭐ Diferenciais da API

# ✅ Sistema Inteligente de Alertas

A API gera alertas automáticos com base nas informações clínicas do pet.

### Exemplos

- vacina atrasada
- check-up geriátrico recomendado
- tratamento abandonado
- obesidade
- retorno pendente

### Endpoint

```http
GET /alertas/pet/{id}
```

---

# ✅ Score de Risco do Pet

O sistema calcula automaticamente o nível de risco do pet com base em:

- idade
- vacinas atrasadas
- obesidade
- frequência de consultas
- tratamentos pendentes

### Níveis

- BAIXO
- MÉDIO
- ALTO

### Endpoint

```http
GET /pets/risco/{id}
```

---

# ✅ Recomendações Preventivas

O sistema recomenda cuidados específicos de acordo com a fase da vida do pet.

### Filhote

- reforço vacinal
- vermífugo

### Adulto

- controle de peso
- check-up anual

### Idoso

- exames cardíacos
- acompanhamento articular

### Endpoint

```http
GET /recomendacoes/pet/{id}
```

---

# ✅ Histórico Longitudinal do Pet

A API fornece uma visão completa da jornada de saúde do pet.

### Informações exibidas

- consultas
- vacinas
- tratamentos
- alertas
- nível de risco

### Endpoint

```http
GET /pets/{id}/historico
```

---

# ✅ Dashboard Clínico

O sistema fornece métricas gerais para clínicas veterinárias.

### Exemplos

- pets em risco
- vacinas atrasadas
- tratamentos ativos
- taxa de adesão

### Endpoint

```http
GET /dashboard
```

---

# 📂 Estrutura do Projeto

```text
src/main/java/com/clyvocare

├── controller
├── service
├── repository
├── dto
├── entity
├── exception
└── config
```

---

# 🐾 Objetivo Final

O objetivo do **ClyvoCare OS** é transformar a medicina veterinária preventiva através de:

- inteligência clínica
- automação
- monitoramento contínuo
- centralização de dados
- experiência digital moderna

Criando uma solução escalável para clínicas veterinárias do futuro.

---
