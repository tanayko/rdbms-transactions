CREATE TABLE people
(
    id bigserial NOT NULL PRIMARY KEY,
    name text UNIQUE NOT NULL,
    age integer NOT NULL
);