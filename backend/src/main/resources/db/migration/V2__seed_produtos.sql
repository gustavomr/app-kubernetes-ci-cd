INSERT INTO produto (nome, preco) VALUES
    ('Teclado Mecânico', 299.90),
    ('Mouse Gamer', 189.50),
    ('Monitor 27 Polegadas', 1599.00),
    ('Headset Bluetooth', 349.90),
    ('Webcam Full HD', 259.99)
ON CONFLICT DO NOTHING;
