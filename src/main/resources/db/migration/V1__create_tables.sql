CREATE TABLE category (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name             VARCHAR(100) NOT NULL UNIQUE,
    image_url        VARCHAR(500),
    image_public_id  VARCHAR(255),
    color            VARCHAR(20)
);


CREATE TABLE question (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    category_id      BIGINT NOT NULL REFERENCES category (id) ON DELETE RESTRICT,
    question_text    TEXT NOT NULL,
    type             VARCHAR(20) NOT NULL CHECK (type IN ('TRUE_FALSE', 'MULTIPLE_CHOICE')),
    explanation      TEXT,
    image_url        VARCHAR(500),
    image_public_id  VARCHAR(255)
);


CREATE INDEX idx_question_category_id ON question (category_id);


CREATE TABLE question_option (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    question_id  BIGINT NOT NULL REFERENCES question (id) ON DELETE CASCADE,
    option_text  VARCHAR(500) NOT NULL,
    is_correct   BOOLEAN NOT NULL DEFAULT FALSE
);


CREATE INDEX idx_question_option_question_id ON question_option (question_id);


CREATE TABLE content_version (
    id       SMALLINT PRIMARY KEY CHECK (id = 1),
    version  BIGINT NOT NULL
);



INSERT INTO content_version (id, version) VALUES (1, 1);