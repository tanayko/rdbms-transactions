CREATE TABLE orders
(
    id bigserial NOT NULL PRIMARY KEY,
    product text NOT NULL,
    cost decimal(20, 4) NOT NULL,
    userId bigint NOT NULL,
    FOREIGN KEY (userId) REFERENCES people (id)
);