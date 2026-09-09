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
