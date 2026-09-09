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
