CREATE SEQUENCE seq_users START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE TABLE users (
                       id             NUMBER(19) PRIMARY KEY,
                       username       VARCHAR2(150) NOT NULL,
                       password  VARCHAR2(255) NOT NULL,
                       role           VARCHAR2(20)  NOT NULL,
                       created_at     TIMESTAMP    NOT NULL,
                       CONSTRAINT uq_users_username UNIQUE (username),
                       CONSTRAINT ck_users_role CHECK (role IN ('OWNER', 'VETERINARIAN', 'CLINIC_ADMIN'))
);CREATE SEQUENCE seq_exams START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE TABLE exams (
    id              NUMBER(19) PRIMARY KEY,
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
CREATE SEQUENCE seq_treatments START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE TABLE treatments (
    id              NUMBER(19) PRIMARY KEY,
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
CREATE SEQUENCE seq_cities START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE TABLE cities (
                        id      NUMBER(19) PRIMARY KEY,
                        name    VARCHAR2(100) NOT NULL,
                        state   VARCHAR2(2)   NOT NULL,
                        region  VARCHAR2(20)  NOT NULL
);CREATE SEQUENCE seq_species START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE TABLE species (
                         id           NUMBER(19) PRIMARY KEY,
                         name         VARCHAR2(50)  NOT NULL,
                         description  VARCHAR2(255),

                         CONSTRAINT uq_species_name UNIQUE (name)
);CREATE SEQUENCE seq_owners START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE TABLE owners (
                        id             NUMBER(19) PRIMARY KEY,
                        name           VARCHAR2(150) NOT NULL,
                        phone          VARCHAR2(20)  NOT NULL,
                        document       VARCHAR2(11)  NOT NULL,
                        registered_at  DATE         NOT NULL,
                        user_id        NUMBER(19)       NOT NULL,
                        city_id        NUMBER(19),

                        CONSTRAINT uq_owners_document UNIQUE (document),
                        CONSTRAINT uq_owners_user_id UNIQUE (user_id),
                        CONSTRAINT fk_owners_user FOREIGN KEY (user_id) REFERENCES users (id),
                        CONSTRAINT fk_owners_city FOREIGN KEY (city_id) REFERENCES cities (id)
);CREATE SEQUENCE seq_pets START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE TABLE pets (
    id             NUMBER(19) PRIMARY KEY,
    name           VARCHAR2(100)  NOT NULL,
    birth_date     DATE,
    sex            VARCHAR2(10),
    current_weight NUMBER(6,3),
    microchip      VARCHAR2(20),
    breed          VARCHAR2(100),
    pet_size       VARCHAR2(10),
    photo_url      VARCHAR2(500),
    registered_at  DATE           NOT NULL,
    owner_id       NUMBER(19)     NOT NULL,
    species_id     NUMBER(19)     NOT NULL,

    CONSTRAINT uq_pets_microchip UNIQUE (microchip),
    CONSTRAINT ck_pets_sex CHECK (sex IN ('MALE', 'FEMALE', 'UNKNOWN')),
    CONSTRAINT ck_pets_size CHECK (pet_size IN ('SMALL', 'MEDIUM', 'LARGE')),
    CONSTRAINT fk_pets_owner FOREIGN KEY (owner_id) REFERENCES owners (id),
    CONSTRAINT fk_pets_species FOREIGN KEY (species_id) REFERENCES species (id)
);CREATE SEQUENCE seq_catalog_items START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE TABLE catalog_items (
    id                     NUMBER(19) PRIMARY KEY,
    name                   VARCHAR2(150) NOT NULL,
    manufacturer           VARCHAR2(100),
    type                   VARCHAR2(20)  NOT NULL,
    active_ingredient      VARCHAR2(150),
    diseases_prevented     VARCHAR2(300),
    booster_interval_days  NUMBER(10),
    species_id             NUMBER(19),

    CONSTRAINT ck_catalog_items_type CHECK (type IN ('VACCINE', 'MEDICATION')),
    CONSTRAINT fk_catalog_items_species FOREIGN KEY (species_id) REFERENCES species (id)
);CREATE SEQUENCE seq_clinics START WITH 1 INCREMENT BY 1 NOCACHE;

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
CREATE SEQUENCE seq_appointments START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE TABLE appointments (
    id                NUMBER(19) PRIMARY KEY,
    appointment_date  DATE          NOT NULL,
    type              VARCHAR2(20),
    reason            VARCHAR2(500),
    diagnosis         VARCHAR2(1000),
    notes             CLOB,
    weight_at_visit   NUMBER(6,3),
    status            VARCHAR2(20),
    pet_id            NUMBER(19)    NOT NULL,
    veterinarian_id   NUMBER(19),
    clinic_id         NUMBER(19),

    CONSTRAINT ck_appointments_type CHECK (type IN ('ROUTINE', 'FOLLOW_UP', 'EMERGENCY')),
    CONSTRAINT ck_appointments_status CHECK (status IN ('SCHEDULED', 'COMPLETED', 'CANCELED')),
    CONSTRAINT fk_appointments_pet FOREIGN KEY (pet_id) REFERENCES pets (id),
    CONSTRAINT fk_appointments_vet FOREIGN KEY (veterinarian_id) REFERENCES veterinarians (id),
    CONSTRAINT fk_appointments_clinic FOREIGN KEY (clinic_id) REFERENCES clinics (id)
);
