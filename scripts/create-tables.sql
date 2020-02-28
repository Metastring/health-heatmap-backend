CREATE TABLE IF NOT EXISTS entities (
    id bigint PRIMARY KEY,
    name VARCHAR(300) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS indicators (
    id bigint PRIMARY KEY,
    name VARCHAR(300) NOT NULL,
    short_code VARCHAR(50) UNIQUE,
    derivation VARCHAR(4000)
);
