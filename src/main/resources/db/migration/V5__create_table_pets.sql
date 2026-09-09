CREATE SEQUENCE seq_pets START WITH 1 INCREMENT BY 1 NOCACHE;
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
);