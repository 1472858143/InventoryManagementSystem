USE supermarket_inventory;

INSERT INTO stock (
    product_id,
    quantity,
    shelf_status,
    min_stock,
    max_stock
)
SELECT
    p.id,
    0,
    '未上架',
    0,
    999999
FROM product p
LEFT JOIN stock s ON s.product_id = p.id
WHERE s.product_id IS NULL;
