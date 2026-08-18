-- Run as a MySQL account allowed to create databases and users.
-- Replace the placeholder password before executing outside local development.
CREATE DATABASE IF NOT EXISTS map_vendor
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

CREATE USER IF NOT EXISTS 'map_vendor_app'@'localhost' IDENTIFIED BY 'CHANGE_ME_STRONG_PASSWORD';
ALTER USER 'map_vendor_app'@'localhost' IDENTIFIED BY 'CHANGE_ME_STRONG_PASSWORD';
GRANT SELECT, INSERT, UPDATE, DELETE ON map_vendor.* TO 'map_vendor_app'@'localhost';

CREATE USER IF NOT EXISTS 'map_vendor_flyway'@'localhost' IDENTIFIED BY 'CHANGE_ME_MIGRATION_PASSWORD';
ALTER USER 'map_vendor_flyway'@'localhost' IDENTIFIED BY 'CHANGE_ME_MIGRATION_PASSWORD';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, DROP, REFERENCES ON map_vendor.* TO 'map_vendor_flyway'@'localhost';
FLUSH PRIVILEGES;

