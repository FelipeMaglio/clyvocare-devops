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
