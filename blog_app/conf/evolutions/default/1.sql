# postgres schema

# --- !Ups

CREATE TABLE users
(
    id character varying(30) COLLATE pg_catalog."default" NOT NULL,
    name character varying(20) COLLATE pg_catalog."default",
    pass character(60) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT users_pkey PRIMARY KEY (id)
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

# --- !Downs

DROP TABLE users;