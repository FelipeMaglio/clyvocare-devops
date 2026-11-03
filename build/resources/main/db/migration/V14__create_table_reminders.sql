CREATE SEQUENCE seq_reminders START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE TABLE reminders (
    id          NUMBER(19)    PRIMARY KEY,
    type        VARCHAR2(20),
    event_date  DATE          NOT NULL,
    message     VARCHAR2(500) NOT NULL,
    status      VARCHAR2(15),
    channel     VARCHAR2(15),
    pet_id      NUMBER(19)    NOT NULL,
    owner_id    NUMBER(19)    NOT NULL,

    CONSTRAINT ck_reminders_type CHECK (type IN ('VACCINE', 'APPOINTMENT', 'EXAM', 'MEDICATION')),
    CONSTRAINT ck_reminders_status CHECK (status IN ('PENDING', 'SENT', 'CONFIRMED')),
    CONSTRAINT ck_reminders_channel CHECK (channel IN ('WHATSAPP', 'PUSH', 'EMAIL')),
    CONSTRAINT fk_reminders_pet FOREIGN KEY (pet_id) REFERENCES pets (id),
    CONSTRAINT fk_reminders_owner FOREIGN KEY (owner_id) REFERENCES owners (id)
);
