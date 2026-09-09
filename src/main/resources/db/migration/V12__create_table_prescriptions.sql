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
