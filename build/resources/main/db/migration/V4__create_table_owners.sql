CREATE SEQUENCE seq_owners START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE TABLE owners (
                        id             NUMBER(19) PRIMARY KEY,
                        name           VARCHAR2(150) NOT NULL,
                        phone          VARCHAR2(20)  NOT NULL,
                        document       VARCHAR2(11)  NOT NULL,
                        registered_at  DATE         NOT NULL,
                        user_id        NUMBER(19)       NOT NULL,
                        city_id        NUMBER(19),

                        CONSTRAINT uq_owners_document UNIQUE (document),
                        CONSTRAINT uq_owners_user_id UNIQUE (user_id),
                        CONSTRAINT fk_owners_user FOREIGN KEY (user_id) REFERENCES users (id),
                        CONSTRAINT fk_owners_city FOREIGN KEY (city_id) REFERENCES cities (id)
);