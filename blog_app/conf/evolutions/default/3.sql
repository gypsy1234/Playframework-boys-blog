# postgres schema

# --- !Ups

ALTER TABLE blog_posts ALTER COLUMN body TYPE varchar(30000);

# --- !Downs

ALTER TABLE blog_posts ALTER COLUMN body TYPE varchar(3000);