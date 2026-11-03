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
