CREATE TABLE users (
    id    bigint  PRIMARY KEY,
    login varchar NOT NULL,
    type  varchar NOT NULL
);

CREATE TABLE repositories (
    id        bigint  PRIMARY KEY,
    owner_id  bigint  NOT NULL,
    name      varchar NOT NULL,
    CONSTRAINT fk_users FOREIGN KEY(owner_id) REFERENCES users(id)
);
