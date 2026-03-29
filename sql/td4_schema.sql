CREATE TYPE mouvement_type AS ENUM ('IN', 'OUT');

CREATE TABLE IF NOT EXISTS stock_movement (
    id SERIAL PRIMARY KEY,
    id_ingredient INT NOT NULL REFERENCES ingredient(id),
    quantity NUMERIC NOT NULL,
    type mouvement_type NOT NULL,
    unit unit_type NOT NULL,
    creation_datetime TIMESTAMP NOT NULL
);