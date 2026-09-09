CREATE SEQUENCE seq_users START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE TABLE users (
                       id             NUMBER(19) PRIMARY KEY,
                       username       VARCHAR2(150) NOT NULL,
                       password  VARCHAR2(255) NOT NULL,
                       role           VARCHAR2(20)  NOT NULL,
                       created_at     TIMESTAMP    NOT NULL,
                       CONSTRAINT uq_users_username UNIQUE (username),
                       CONSTRAINT ck_users_role CHECK (role IN ('OWNER', 'VETERINARIAN', 'CLINIC_ADMIN'))
);