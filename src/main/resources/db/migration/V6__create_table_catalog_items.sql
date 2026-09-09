CREATE SEQUENCE seq_catalog_items START WITH 1 INCREMENT BY 1 NOCACHE;

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
);