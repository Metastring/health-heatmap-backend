CREATE TABLE IF NOT EXISTS entities (
    id serial PRIMARY KEY,
    name VARCHAR(300) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS indicators (
    id SERIAL PRIMARY KEY,
    name VARCHAR(300) UNIQUE NOT NULL,
    formula JSON,
    present_in JSON
);

CREATE TABLE IF NOT EXISTS dataset_metadata (
    id serial PRIMARY KEY,
    name VARCHAR(300) UNIQUE NOT NULL,
    table_name VARCHAR(300) UNIQUE NOT NULL
);

