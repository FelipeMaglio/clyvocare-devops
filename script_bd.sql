-- =============================================================================
-- ClyvoCare OS - DDL consolidado do banco de dados (Oracle)
-- Gerado a partir das 14 migrations do Flyway (src/main/resources/db/migration)
-- Ordem ajustada para respeitar as dependencias de chave estrangeira (FK)
-- =============================================================================

-- -----------------------------------------------------------------------------
-- USERS: credenciais de autenticacao (login/senha/role). Toda pessoa que acessa
-- o sistema (tutor ou veterinario) tem um registro aqui; OWNERS e VETERINARIANS
-- guardam os dados de perfil e apontam de volta pra essa tabela.
-- -----------------------------------------------------------------------------
CREATE SEQUENCE seq_users START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE TABLE users (
    id             NUMBER(19) PRIMARY KEY,
    username       VARCHAR2(150) NOT NULL,
    password       VARCHAR2(255) NOT NULL,
    role           VARCHAR2(20)  NOT NULL,
    created_at     TIMESTAMP     NOT NULL,
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT ck_users_role CHECK (role IN ('OWNER', 'VETERINARIAN', 'CLINIC_ADMIN'))
);
COMMENT ON TABLE users IS 'Credenciais de acesso (login) de tutores e veterinarios';
COMMENT ON COLUMN users.role IS 'Perfil de acesso: define o que o usuario pode fazer na API';

-- -----------------------------------------------------------------------------
-- CITIES: cadastro auxiliar de cidade/estado, usado por owners e clinics.
-- -----------------------------------------------------------------------------
CREATE SEQUENCE seq_cities START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE TABLE cities (
    id      NUMBER(19) PRIMARY KEY,
    name    VARCHAR2(100) NOT NULL,
    state   VARCHAR2(2)   NOT NULL,
    region  VARCHAR2(20)  NOT NULL
);
COMMENT ON TABLE cities IS 'Cadastro auxiliar de cidades, referenciado por tutores e clinicas';

-- -----------------------------------------------------------------------------
-- SPECIES: especies de pet (cachorro, gato, etc). Referenciada por PETS e por
-- CATALOG_ITEMS (uma vacina/medicamento pode ser especifica de uma especie).
-- -----------------------------------------------------------------------------
CREATE SEQUENCE seq_species START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE TABLE species (
    id           NUMBER(19) PRIMARY KEY,
    name         VARCHAR2(50)  NOT NULL,
    description  VARCHAR2(255),
    CONSTRAINT uq_species_name UNIQUE (name)
);
COMMENT ON TABLE species IS 'Especies de animais atendidas pela clinica (cachorro, gato, etc)';

-- -----------------------------------------------------------------------------
-- CLINICS: clinicas veterinarias parceiras, onde os veterinarios atuam e as
-- consultas acontecem.
-- -----------------------------------------------------------------------------
CREATE SEQUENCE seq_clinics START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE TABLE clinics (
    id                  NUMBER(19) PRIMARY KEY,
    name                VARCHAR2(150) NOT NULL,
    tax_id              VARCHAR2(14)  NOT NULL,
    phone               VARCHAR2(20),
    email               VARCHAR2(150),
    address             VARCHAR2(300),
    subscription_plan   VARCHAR2(20),
    subscription_date   DATE,
    city_id             NUMBER(19),
    CONSTRAINT uq_clinics_tax_id UNIQUE (tax_id),
    CONSTRAINT fk_clinics_city FOREIGN KEY (city_id) REFERENCES cities (id)
);
COMMENT ON TABLE clinics IS 'Clinicas veterinarias parceiras da plataforma';
COMMENT ON COLUMN clinics.tax_id IS 'CNPJ da clinica';

-- -----------------------------------------------------------------------------
-- OWNERS (TUTOR): dono do pet. Vinculado a um USER (login) e opcionalmente a
-- uma cidade. Tabela CORE do negocio - e o ponto de entrada de todo o cadastro.
-- -----------------------------------------------------------------------------
CREATE SEQUENCE seq_owners START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE TABLE owners (
    id             NUMBER(19) PRIMARY KEY,
    name           VARCHAR2(150) NOT NULL,
    phone          VARCHAR2(20)  NOT NULL,
    document       VARCHAR2(11)  NOT NULL,
    registered_at  DATE          NOT NULL,
    user_id        NUMBER(19)    NOT NULL,
    city_id        NUMBER(19),
    CONSTRAINT uq_owners_document UNIQUE (document),
    CONSTRAINT uq_owners_user_id UNIQUE (user_id),
    CONSTRAINT fk_owners_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_owners_city FOREIGN KEY (city_id) REFERENCES cities (id)
);
COMMENT ON TABLE owners IS 'Tutor do pet - tabela nucleo do negocio';
COMMENT ON COLUMN owners.document IS 'CPF do tutor';
COMMENT ON COLUMN owners.user_id IS 'Vinculo 1-para-1 com a conta de login em USERS';

-- -----------------------------------------------------------------------------
-- VETERINARIANS: profissional veterinario. Vinculado a um USER (login) e
-- opcionalmente a uma clinica.
-- -----------------------------------------------------------------------------
CREATE SEQUENCE seq_veterinarians START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE TABLE veterinarians (
    id              NUMBER(19) PRIMARY KEY,
    name            VARCHAR2(150) NOT NULL,
    license_number  VARCHAR2(20)  NOT NULL,
    specialty       VARCHAR2(100),
    email           VARCHAR2(150),
    phone           VARCHAR2(20),
    user_id         NUMBER(19)    NOT NULL,
    clinic_id       NUMBER(19),
    CONSTRAINT uq_veterinarians_license UNIQUE (license_number),
    CONSTRAINT uq_veterinarians_user_id UNIQUE (user_id),
    CONSTRAINT fk_veterinarians_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_veterinarians_clinic FOREIGN KEY (clinic_id) REFERENCES clinics (id)
);
COMMENT ON TABLE veterinarians IS 'Profissional veterinario, vinculado a uma clinica';
COMMENT ON COLUMN veterinarians.license_number IS 'Numero do CRMV (registro profissional)';

-- -----------------------------------------------------------------------------
-- PETS: animal cadastrado por um tutor. Tabela CORE do negocio - e em cima
-- dela que todo o historico clinico (appointments, exams, treatments,
-- vaccinations) e construido. Usada no CRUD principal desta entrega.
-- -----------------------------------------------------------------------------
CREATE SEQUENCE seq_pets START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE TABLE pets (
    id             NUMBER(19)    PRIMARY KEY,
    name           VARCHAR2(100) NOT NULL,
    birth_date     DATE,
    sex            VARCHAR2(10),
    current_weight NUMBER(6,3),
    microchip      VARCHAR2(20),
    breed          VARCHAR2(100),
    pet_size       VARCHAR2(10),
    photo_url      VARCHAR2(500),
    registered_at  DATE          NOT NULL,
    owner_id       NUMBER(19)    NOT NULL,
    species_id     NUMBER(19)    NOT NULL,
    CONSTRAINT uq_pets_microchip UNIQUE (microchip),
    CONSTRAINT ck_pets_sex CHECK (sex IN ('MALE', 'FEMALE', 'UNKNOWN')),
    CONSTRAINT ck_pets_size CHECK (pet_size IN ('SMALL', 'MEDIUM', 'LARGE')),
    CONSTRAINT fk_pets_owner FOREIGN KEY (owner_id) REFERENCES owners (id),
    CONSTRAINT fk_pets_species FOREIGN KEY (species_id) REFERENCES species (id)
);
COMMENT ON TABLE pets IS 'Pet cadastrado por um tutor - tabela nucleo, usada no CRUD principal da entrega';
COMMENT ON COLUMN pets.microchip IS 'Numero do microchip de identificacao, se houver';
COMMENT ON COLUMN pets.owner_id IS 'Tutor responsavel pelo pet (FK para OWNERS)';
COMMENT ON COLUMN pets.species_id IS 'Especie do pet (FK para SPECIES)';

-- -----------------------------------------------------------------------------
-- APPOINTMENTS: consulta veterinaria de um pet. Pode gerar exames, tratamentos
-- e vacinacoes associados.
-- -----------------------------------------------------------------------------
CREATE SEQUENCE seq_appointments START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE TABLE appointments (
    id                NUMBER(19)     PRIMARY KEY,
    appointment_date  DATE           NOT NULL,
    type              VARCHAR2(20),
    reason            VARCHAR2(500),
    diagnosis         VARCHAR2(1000),
    notes             CLOB,
    weight_at_visit   NUMBER(6,3),
    status            VARCHAR2(20),
    pet_id            NUMBER(19)     NOT NULL,
    veterinarian_id   NUMBER(19),
    clinic_id         NUMBER(19),
    CONSTRAINT ck_appointments_type CHECK (type IN ('ROUTINE', 'FOLLOW_UP', 'EMERGENCY')),
    CONSTRAINT ck_appointments_status CHECK (status IN ('SCHEDULED', 'COMPLETED', 'CANCELED')),
    CONSTRAINT fk_appointments_pet FOREIGN KEY (pet_id) REFERENCES pets (id),
    CONSTRAINT fk_appointments_vet FOREIGN KEY (veterinarian_id) REFERENCES veterinarians (id),
    CONSTRAINT fk_appointments_clinic FOREIGN KEY (clinic_id) REFERENCES clinics (id)
);
COMMENT ON TABLE appointments IS 'Consulta veterinaria de um pet';
COMMENT ON COLUMN appointments.status IS 'Situacao da consulta: agendada, concluida ou cancelada';

-- -----------------------------------------------------------------------------
-- EXAMS: exame realizado em um pet, opcionalmente vinculado a uma consulta.
-- Tabela CORE do negocio - segunda tabela do CRUD principal desta entrega,
-- relacionada a PETS por chave estrangeira (pet_id).
-- -----------------------------------------------------------------------------
CREATE SEQUENCE seq_exams START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE TABLE exams (
    id              NUMBER(19)    PRIMARY KEY,
    exam_type       VARCHAR2(100) NOT NULL,
    request_date    DATE          NOT NULL,
    result_date     DATE,
    result          CLOB,
    file_url        VARCHAR2(500),
    laboratory      VARCHAR2(150),
    pet_id          NUMBER(19)    NOT NULL,
    appointment_id  NUMBER(19),
    CONSTRAINT fk_exams_pet FOREIGN KEY (pet_id) REFERENCES pets (id),
    CONSTRAINT fk_exams_appointment FOREIGN KEY (appointment_id) REFERENCES appointments (id)
);
COMMENT ON TABLE exams IS 'Exame do pet - tabela nucleo, usada no CRUD principal da entrega (relacionada a PETS)';
COMMENT ON COLUMN exams.pet_id IS 'Pet ao qual o exame pertence (FK para PETS)';
COMMENT ON COLUMN exams.result IS 'Laudo/resultado do exame, preenchido apos a realizacao';

-- -----------------------------------------------------------------------------
-- TREATMENTS: tratamento prescrito a um pet, opcionalmente vinculado a uma
-- consulta. Pode ter varias prescricoes associadas.
-- -----------------------------------------------------------------------------
CREATE SEQUENCE seq_treatments START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE TABLE treatments (
    id              NUMBER(19)    PRIMARY KEY,
    description     VARCHAR2(500) NOT NULL,
    start_date      DATE          NOT NULL,
    end_date        DATE,
    status          VARCHAR2(20),
    pet_id          NUMBER(19)    NOT NULL,
    appointment_id  NUMBER(19),
    CONSTRAINT ck_treatments_status CHECK (status IN ('ACTIVE', 'COMPLETED', 'SUSPENDED')),
    CONSTRAINT fk_treatments_pet FOREIGN KEY (pet_id) REFERENCES pets (id),
    CONSTRAINT fk_treatments_appointment FOREIGN KEY (appointment_id) REFERENCES appointments (id)
);
COMMENT ON TABLE treatments IS 'Tratamento prescrito a um pet, geralmente decorrente de uma consulta';

-- -----------------------------------------------------------------------------
-- CATALOG_ITEMS: catalogo padronizado de vacinas e medicamentos, usado por
-- PRESCRIPTIONS e VACCINATIONS.
-- -----------------------------------------------------------------------------
CREATE SEQUENCE seq_catalog_items START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE TABLE catalog_items (
    id                     NUMBER(19)    PRIMARY KEY,
    name                   VARCHAR2(150) NOT NULL,
    manufacturer           VARCHAR2(100),
    type                   VARCHAR2(20)  NOT NULL,
    active_ingredient      VARCHAR2(150),
    diseases_prevented     VARCHAR2(300),
    booster_interval_days  NUMBER(10),
    species_id             NUMBER(19),
    CONSTRAINT ck_catalog_items_type CHECK (type IN ('VACCINE', 'MEDICATION')),
    CONSTRAINT fk_catalog_items_species FOREIGN KEY (species_id) REFERENCES species (id)
);
COMMENT ON TABLE catalog_items IS 'Catalogo padronizado de vacinas e medicamentos';
COMMENT ON COLUMN catalog_items.booster_interval_days IS 'Intervalo em dias ate o reforco, usado no calculo automatico de vacinacao';

-- -----------------------------------------------------------------------------
-- PRESCRIPTIONS: item do catalogo (medicamento) prescrito dentro de um
-- tratamento, com dosagem e frequencia especificas.
-- -----------------------------------------------------------------------------
CREATE SEQUENCE seq_prescriptions START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE TABLE prescriptions (
    id               NUMBER(19)   PRIMARY KEY,
    dosage           VARCHAR2(50) NOT NULL,
    frequency        VARCHAR2(50) NOT NULL,
    duration_days    NUMBER(10),
    instructions     VARCHAR2(500),
    treatment_id     NUMBER(19)   NOT NULL,
    catalog_item_id  NUMBER(19)   NOT NULL,
    CONSTRAINT fk_prescriptions_treatment FOREIGN KEY (treatment_id) REFERENCES treatments (id),
    CONSTRAINT fk_prescriptions_catalog_item FOREIGN KEY (catalog_item_id) REFERENCES catalog_items (id)
);
COMMENT ON TABLE prescriptions IS 'Prescricao de um item do catalogo dentro de um tratamento';

-- -----------------------------------------------------------------------------
-- VACCINATIONS: aplicacao de vacina em um pet, com calculo automatico da
-- proxima dose (next_dose_date) feito pela aplicacao.
-- -----------------------------------------------------------------------------
CREATE SEQUENCE seq_vaccinations START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE TABLE vaccinations (
    id                NUMBER(19) PRIMARY KEY,
    application_date  DATE       NOT NULL,
    batch             VARCHAR2(50),
    next_dose_date    DATE,
    expiration_date   DATE,
    pet_id            NUMBER(19) NOT NULL,
    catalog_item_id   NUMBER(19) NOT NULL,
    appointment_id    NUMBER(19),
    veterinarian_id   NUMBER(19),
    CONSTRAINT fk_vaccinations_pet FOREIGN KEY (pet_id) REFERENCES pets (id),
    CONSTRAINT fk_vaccinations_catalog_item FOREIGN KEY (catalog_item_id) REFERENCES catalog_items (id),
    CONSTRAINT fk_vaccinations_appointment FOREIGN KEY (appointment_id) REFERENCES appointments (id),
    CONSTRAINT fk_vaccinations_vet FOREIGN KEY (veterinarian_id) REFERENCES veterinarians (id)
);
COMMENT ON TABLE vaccinations IS 'Aplicacao de vacina em um pet';
COMMENT ON COLUMN vaccinations.next_dose_date IS 'Data do proximo reforco, calculada automaticamente pela aplicacao';

-- -----------------------------------------------------------------------------
-- REMINDERS: lembrete automatico gerado para o tutor (retorno de consulta,
-- reforco de vacina, etc).
-- -----------------------------------------------------------------------------
CREATE SEQUENCE seq_reminders START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE TABLE reminders (
    id          NUMBER(19)    PRIMARY KEY,
    type        VARCHAR2(20),
    event_date  DATE          NOT NULL,
    message     VARCHAR2(500) NOT NULL,
    status      VARCHAR2(15),
    channel     VARCHAR2(15),
    pet_id      NUMBER(19)    NOT NULL,
    owner_id    NUMBER(19)    NOT NULL,
    CONSTRAINT ck_reminders_type CHECK (type IN ('VACCINE', 'APPOINTMENT', 'EXAM', 'MEDICATION')),
    CONSTRAINT ck_reminders_status CHECK (status IN ('PENDING', 'SENT', 'CONFIRMED')),
    CONSTRAINT ck_reminders_channel CHECK (channel IN ('WHATSAPP', 'PUSH', 'EMAIL')),
    CONSTRAINT fk_reminders_pet FOREIGN KEY (pet_id) REFERENCES pets (id),
    CONSTRAINT fk_reminders_owner FOREIGN KEY (owner_id) REFERENCES owners (id)
);
COMMENT ON TABLE reminders IS 'Lembrete automatico enviado ao tutor (retorno de consulta, reforco de vacina, etc)';