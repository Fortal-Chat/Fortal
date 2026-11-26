CREATE UNIQUE INDEX users_username_lower_idx ON users (lower(username));

CREATE UNIQUE INDEX users_email_lower_idx ON users (lower(email));

ALTER TABLE users DROP CONSTRAINT users_username_key;

ALTER TABLE users DROP CONSTRAINT users_email_key;
