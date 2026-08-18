-- Read-only checks to archive before V002. Stop if values or dependencies differ from V001.
SELECT cancel_source, COUNT(*) AS row_count
FROM reserve_order
GROUP BY cancel_source
ORDER BY cancel_source;

SELECT operator_type, COUNT(*) AS row_count
FROM order_status_log
GROUP BY operator_type
ORDER BY operator_type;

SELECT COUNT(*) AS sys_admin_count FROM sys_admin;
SELECT COUNT(*) AS merchant_account_count FROM merchant_account;

SELECT id, order_no, cancel_reason
FROM reserve_order
WHERE cancel_source = 'MERCHANT'
ORDER BY id;

SELECT id, order_id, reason
FROM order_status_log
WHERE operator_type = 'MERCHANT'
ORDER BY id;

SELECT table_name, constraint_name, referenced_table_name
FROM information_schema.referential_constraints
WHERE constraint_schema = DATABASE()
  AND (table_name IN ('sys_admin', 'merchant_account')
       OR referenced_table_name IN ('sys_admin', 'merchant_account'))
ORDER BY table_name, constraint_name;

SELECT trigger_name, event_object_table
FROM information_schema.triggers
WHERE trigger_schema = DATABASE()
  AND event_object_table IN ('sys_admin', 'merchant_account');
