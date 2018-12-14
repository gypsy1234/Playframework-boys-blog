# postgres schema

# --- !Ups

CREATE TABLE blog_posts
(
    id uuid NOT NULL,
    create_user_id character varying(30) COLLATE pg_catalog."default",
    title character varying(100) COLLATE pg_catalog."default" NOT NULL,
    body character varying(3000) COLLATE pg_catalog."default" NOT NULL,
    is_draft boolean NOT NULL,
    created_date timestamp  NOT NULL,
    updated_date timestamp ,
    CONSTRAINT blog_posts_pkey PRIMARY KEY (id),
    CONSTRAINT create_user_fk FOREIGN KEY (create_user_id)
        REFERENCES public.users (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

# --- !Downs

DROP TABLE blog_posts;