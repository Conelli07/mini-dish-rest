ALTER TABLE dish ADD COLUMN IF NOT EXISTS selling_price NUMERIC;

CREATE TYPE unit_type AS ENUM ('PCS', 'KG', 'L');

CREATE TABLE IF NOT EXISTS dish_ingredient (
    id SERIAL PRIMARY KEY,
    id_dish INT NOT NULL REFERENCES dish(id),
    id_ingredient INT NOT NULL REFERENCES ingredient(id),
    quantity_required NUMERIC NOT NULL,
    unit unit_type NOT NULL
);