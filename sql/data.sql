INSERT INTO dish (id, name, dish_type, selling_price) VALUES
    (1, 'Salade fraîche',     'START',   3500.00),
    (2, 'Poulet grillé',      'MAIN',    12000.00),
    (3, 'Riz aux légumes',    'MAIN',    NULL),
    (4, 'Gâteau au chocolat', 'DESSERT', 8000.00),
    (5, 'Salade de fruits',   'DESSERT', NULL);

SELECT setval('dish_id_seq', 5);

INSERT INTO ingredient (id, name, price, category) VALUES
    (1, 'Laitue',   800.00,  'VEGETABLE'),
    (2, 'Tomate',   600.00,  'VEGETABLE'),
    (3, 'Poulet',   4500.00, 'ANIMAL'),
    (4, 'Chocolat', 3000.00, 'OTHER'),
    (5, 'Beurre',   2500.00, 'DAIRY');

SELECT setval('ingredient_id_seq', 5);

INSERT INTO dish_ingredient (id, id_dish, id_ingredient, quantity_required, unit) VALUES
    (1, 1, 1, 0.20, 'KG'),
    (2, 1, 2, 0.15, 'KG'),
    (3, 2, 3, 1.00, 'KG'),
    (4, 4, 4, 0.30, 'KG'),
    (5, 4, 5, 0.20, 'KG');

SELECT setval('dish_ingredient_id_seq', 5);

INSERT INTO stock_movement (id, id_ingredient, quantity, type, unit, creation_datetime) VALUES
    (1,  1, 5.00, 'IN',  'KG', '2024-01-05 08:00:00'),
    (2,  2, 4.00, 'IN',  'KG', '2024-01-05 08:00:00'),
    (3,  3, 10.0, 'IN',  'KG', '2024-01-04 09:00:00'),
    (4,  4, 3.00, 'IN',  'KG', '2024-01-05 10:00:00'),
    (5,  5, 2.50, 'IN',  'KG', '2024-01-05 10:00:00'),
    (6,  1, 0.20, 'OUT', 'KG', '2024-01-06 12:00:00'),
    (7,  2, 0.15, 'OUT', 'KG', '2024-01-06 12:00:00'),
    (8,  3, 1.00, 'OUT', 'KG', '2024-01-06 12:00:00'),
    (9,  4, 0.30, 'OUT', 'KG', '2024-01-06 12:00:00'),
    (10, 5, 0.20, 'OUT', 'KG', '2024-01-06 12:00:00');

SELECT setval('stock_movement_id_seq', 10);