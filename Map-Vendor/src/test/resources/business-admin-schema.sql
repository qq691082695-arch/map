DROP TABLE IF EXISTS reserve_order;
DROP TABLE IF EXISTS business;
DROP TABLE IF EXISTS business_file_relation;
DROP TABLE IF EXISTS file_resource;
CREATE TABLE business (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(128) NOT NULL, address VARCHAR(255) NOT NULL,
 longitude DECIMAL(10,7) NOT NULL, latitude DECIMAL(10,7) NOT NULL, business_type VARCHAR(16) NOT NULL,
 intro VARCHAR(1000), food_contact_name VARCHAR(64), food_contact_phone VARCHAR(32), food_recommended_dishes VARCHAR(500),
 status VARCHAR(16) NOT NULL, created_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3),
 updated_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3), deleted_at TIMESTAMP(3)
);
CREATE TABLE reserve_order (id BIGINT PRIMARY KEY, business_id BIGINT NOT NULL,
 business_name_snapshot VARCHAR(128) NOT NULL, CONSTRAINT fk_test_order_business FOREIGN KEY (business_id) REFERENCES business(id));
CREATE TABLE file_resource (id BIGINT PRIMARY KEY, public_url VARCHAR(1024), status VARCHAR(16), deleted_at TIMESTAMP(3));
CREATE TABLE business_file_relation (business_id BIGINT NOT NULL, file_resource_id BIGINT NOT NULL, sort_no INT NOT NULL,
 PRIMARY KEY(business_id,file_resource_id));
