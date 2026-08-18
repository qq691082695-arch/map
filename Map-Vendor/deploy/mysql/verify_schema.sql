SET SESSION sql_mode = 'STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';
SET NAMES utf8mb4;

SELECT VERSION() AS mysql_version,
       @@character_set_database AS database_charset,
       @@collation_database AS database_collation;

SELECT COUNT(*) AS business_table_count_should_be_12
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN (
    'wx_user', 'university', 'business',
    'university_file_relation', 'business_file_relation',
    'business_travel_car', 'business_food_dish', 'business_hotel_room',
    'file_resource', 'reserve_order', 'order_status_log', 'api_idempotency'
  );

SELECT COUNT(*) AS legacy_account_table_count_should_be_0
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN ('sys_admin', 'merchant_account');

SELECT COUNT(*) AS legacy_token_column_count_should_be_0
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND column_name = 'token_version';

SELECT COUNT(*) AS legacy_order_value_count_should_be_0
FROM reserve_order
WHERE cancel_source = 'MERCHANT';

SELECT COUNT(*) AS legacy_log_value_count_should_be_0
FROM order_status_log
WHERE operator_type = 'MERCHANT';

SELECT table_name, engine, table_collation
FROM information_schema.tables
WHERE table_schema = DATABASE()
ORDER BY table_name;

SELECT constraint_name, table_name
FROM information_schema.table_constraints
WHERE constraint_schema = DATABASE()
  AND constraint_type = 'FOREIGN KEY'
ORDER BY table_name, constraint_name;
