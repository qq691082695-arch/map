DROP TABLE IF EXISTS reserve_order;
CREATE TABLE reserve_order (
 id BIGINT PRIMARY KEY, order_no VARCHAR(32) NOT NULL, openid VARCHAR(128) NOT NULL, business_id BIGINT NOT NULL,
 business_name_snapshot VARCHAR(128) NOT NULL, business_type VARCHAR(16) NOT NULL, service_type VARCHAR(16) NOT NULL,
 contact_name VARCHAR(64) NOT NULL, contact_phone VARCHAR(32) NOT NULL, people_num INT NOT NULL, service_date DATE NOT NULL,
 status VARCHAR(16) NOT NULL, car_id BIGINT, car_spec_snapshot VARCHAR(255), car_quantity INT, service_mode VARCHAR(16),
 room_id BIGINT, room_spec_snapshot VARCHAR(255), room_quantity INT, meal_period VARCHAR(16), option_snapshot_json VARCHAR(1000),
 confirmed_at TIMESTAMP(3), cancelled_at TIMESTAMP(3), cancel_source VARCHAR(16), cancel_reason VARCHAR(500),
 version INT NOT NULL DEFAULT 0, created_at TIMESTAMP(3) NOT NULL, updated_at TIMESTAMP(3) NOT NULL
);
DROP TABLE IF EXISTS order_status_log;
CREATE TABLE order_status_log (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, order_id BIGINT NOT NULL, from_status VARCHAR(16), to_status VARCHAR(16) NOT NULL,
 operator_type VARCHAR(16) NOT NULL, operator_id BIGINT, reason VARCHAR(500), request_id VARCHAR(64),
 created_at TIMESTAMP(3) NOT NULL
);
