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
