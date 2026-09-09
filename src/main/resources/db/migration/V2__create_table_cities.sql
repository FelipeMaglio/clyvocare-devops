CREATE SEQUENCE seq_cities START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE TABLE cities (
                        id      NUMBER(19) PRIMARY KEY,
                        name    VARCHAR2(100) NOT NULL,
                        state   VARCHAR2(2)   NOT NULL,
                        region  VARCHAR2(20)  NOT NULL
);