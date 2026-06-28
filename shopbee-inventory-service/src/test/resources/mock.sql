-- Mock data generation for shopbee_reservations
-- 1. Mock data generation for shopbee_inventory
-- Generates 10,000 unique inventory items across 10 tenants
INSERT INTO shopbee_inventory (id, tenant_id, product_id, sku, active, stock_quantity, max_reservation_quantity, reserved_quantity, version, created_at, updated_at)
SELECT
    gen_random_uuid() as id,
    'tenant-' || (floor(random() * 10) + 1)::text as tenant_id,
    'PROD-' || i::text as product_id,
    'SKU-' || i::text || '-' || (floor(random() * 1000))::text as sku,
    true as active,
    (floor(random() * 500) + 50)::bigint as stock_quantity,
    10 as max_reservation_quantity,
    0 as reserved_quantity,
    1 as version,
    NOW() - INTERVAL '30 days' as created_at,
    NOW() - INTERVAL '1 day' as updated_at
FROM generate_series(1, 10000) s(i);

-- 2. Mock data generation for shopbee_inventory_adjustments
-- Generates 3 historical audit records for every inventory item (30,000 records total)
INSERT INTO shopbee_inventory_adjustments (id, inventory_id, tenant_id, previous_quantity, new_quantity, change_quantity, reason, created_by, created_at)
SELECT
    gen_random_uuid() as id,
    inv.id as inventory_id,
    inv.tenant_id,
    (inv.stock_quantity - (s.step * 10))::bigint as previous_quantity,
    (inv.stock_quantity - ((s.step - 1) * 10))::bigint as new_quantity,
    10::bigint as change_quantity,
    CASE 
        WHEN s.step = 1 THEN 'Initial Stock'
        WHEN s.step = 2 THEN 'Restock from Warehouse'
        ELSE 'Cycle Count Correction'
    END as reason,
    'admin-user' as created_by,
    inv.created_at + (s.step * INTERVAL '1 day') as created_at
FROM shopbee_inventory inv
CROSS JOIN generate_series(1, 3) s(step);

-- 3. Mock data generation for shopbee_reservations
-- Generates 100,000 records with randomized tenants, products, and statuses

INSERT INTO shopbee_reservations (id, tenant_id, order_id, product_id, quantity, status, expires_at, created_at)
SELECT
    gen_random_uuid() as id,
    'tenant-' || (floor(random() * 10) + 1)::text as tenant_id, -- 10 different tenants
    'ORD-' || i::text as order_id,
    'PROD-' || (floor(random() * 5000) + 1)::text as product_id, -- 5000 different products
    (floor(random() * 10) + 1)::int as quantity,
    (ARRAY['PENDING', 'CONFIRMED', 'CANCELLED'])[floor(random() * 3) + 1] as status,
    -- Randomized expiry: 50% chance to be in the past (expired) for PENDING status testing
    NOW() + (random() * (INTERVAL '1 hour') - INTERVAL '30 minutes') as expires_at,
    NOW() - (random() * INTERVAL '7 days') as created_at
FROM generate_series(1, 100000) s(i);

-- Analyze the table to update statistics for the query planner
ANALYZE shopbee_inventory;
ANALYZE shopbee_inventory_adjustments;
ANALYZE shopbee_reservations;

-- Validation queries
SELECT 'Inventory Count', count(*) FROM shopbee_inventory;
SELECT 'Adjustment Count', count(*) FROM shopbee_inventory_adjustments;
SELECT status, count(*) FROM shopbee_reservations GROUP BY status;
SELECT count(*) FROM shopbee_reservations WHERE status = 'PENDING' AND expires_at < NOW();