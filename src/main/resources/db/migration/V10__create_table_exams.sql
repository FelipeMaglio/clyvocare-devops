CREATE SEQUENCE seq_exams START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE TABLE exams (
    id              NUMBER(19) PRIMARY KEY,
    exam_type       VARCHAR2(100) NOT NULL,
    request_date    DATE          NOT NULL,
    result_date     DATE,
    result          CLOB,
    file_url        VARCHAR2(500),
    laboratory      VARCHAR2(150),
    pet_id          NUMBER(19)    NOT NULL,
    appointment_id  NUMBER(19),

    CONSTRAINT fk_exams_pet FOREIGN KEY (pet_id) REFERENCES pets (id),
    CONSTRAINT fk_exams_appointment FOREIGN KEY (appointment_id) REFERENCES appointments (id)
);
