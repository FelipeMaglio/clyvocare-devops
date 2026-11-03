CREATE SEQUENCE seq_species START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE TABLE species (
                         id           NUMBER(19) PRIMARY KEY,
                         name         VARCHAR2(50)  NOT NULL,
                         description  VARCHAR2(255),

                         CONSTRAINT uq_species_name UNIQUE (name)
);