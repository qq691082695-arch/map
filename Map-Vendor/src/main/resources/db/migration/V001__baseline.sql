-- Map Vendor database baseline (MySQL Community Server 9.0.1, utf8mb4, strict SQL mode).
-- The database itself must be created before Flyway runs; see deploy/mysql/README.md.

CREATE TABLE sys_admin (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'ENABLED',
    token_version INT UNSIGNED NOT NULL DEFAULT 0,
    last_login_at DATETIME(3) NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_admin_username (username),
    CONSTRAINT ck_sys_admin_status CHECK (status IN ('ENABLED', 'DISABLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE wx_user (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    openid VARCHAR(128) NOT NULL COMMENT 'Frontend-provided identifier; not strong authentication',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    last_seen_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_wx_user_openid (openid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE university (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name VARCHAR(128) NOT NULL,
    intro TEXT NULL,
    polygon_json JSON NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'ENABLED',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    deleted_at DATETIME(3) NULL,
    PRIMARY KEY (id),
    KEY idx_university_status_deleted (status, deleted_at),
    CONSTRAINT ck_university_status CHECK (status IN ('ENABLED', 'DISABLED')),
    CONSTRAINT ck_university_polygon_array CHECK (JSON_TYPE(polygon_json) = 'ARRAY' AND JSON_LENGTH(polygon_json) >= 3)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE business (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name VARCHAR(128) NOT NULL,
    address VARCHAR(255) NOT NULL,
    longitude DECIMAL(10,7) NOT NULL,
    latitude DECIMAL(10,7) NOT NULL,
    business_type VARCHAR(16) NOT NULL,
    intro TEXT NULL,
    food_contact_name VARCHAR(64) NULL,
    food_contact_phone VARCHAR(32) NULL,
    food_recommended_dishes VARCHAR(500) NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'ENABLED',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    deleted_at DATETIME(3) NULL,
    PRIMARY KEY (id),
    KEY idx_business_type_status_deleted (business_type, status, deleted_at),
    CONSTRAINT ck_business_type CHECK (business_type IN ('TRAVEL', 'HOTEL', 'FOOD')),
    CONSTRAINT ck_business_status CHECK (status IN ('ENABLED', 'DISABLED')),
    CONSTRAINT ck_business_longitude CHECK (longitude BETWEEN -180 AND 180),
    CONSTRAINT ck_business_latitude CHECK (latitude BETWEEN -90 AND 90)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE merchant_account (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    business_id BIGINT UNSIGNED NOT NULL,
    username VARCHAR(64) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'ENABLED',
    token_version INT UNSIGNED NOT NULL DEFAULT 0,
    last_login_at DATETIME(3) NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_merchant_account_username (username),
    KEY idx_merchant_account_business_status (business_id, status),
    CONSTRAINT fk_merchant_account_business FOREIGN KEY (business_id) REFERENCES business (id),
    CONSTRAINT ck_merchant_account_status CHECK (status IN ('ENABLED', 'DISABLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE file_resource (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    storage_key VARCHAR(255) NOT NULL,
    public_url VARCHAR(1024) NOT NULL,
    original_name VARCHAR(255) NULL,
    mime_type VARCHAR(64) NOT NULL,
    size_bytes BIGINT UNSIGNED NOT NULL,
    sha256 CHAR(64) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    deleted_at DATETIME(3) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_file_resource_storage_key (storage_key),
    KEY idx_file_resource_sha256 (sha256),
    KEY idx_file_resource_status_deleted (status, deleted_at),
    CONSTRAINT ck_file_resource_mime CHECK (mime_type IN ('image/jpeg', 'image/png', 'image/webp')),
    CONSTRAINT ck_file_resource_size CHECK (size_bytes > 0),
    CONSTRAINT ck_file_resource_status CHECK (status IN ('ACTIVE', 'DISABLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE university_file_relation (
    university_id BIGINT UNSIGNED NOT NULL,
    file_resource_id BIGINT UNSIGNED NOT NULL,
    sort_no INT UNSIGNED NOT NULL DEFAULT 0,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (university_id, file_resource_id),
    KEY idx_university_file_sort (university_id, sort_no, file_resource_id),
    CONSTRAINT fk_university_file_university FOREIGN KEY (university_id) REFERENCES university (id),
    CONSTRAINT fk_university_file_resource FOREIGN KEY (file_resource_id) REFERENCES file_resource (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE business_file_relation (
    business_id BIGINT UNSIGNED NOT NULL,
    file_resource_id BIGINT UNSIGNED NOT NULL,
    sort_no INT UNSIGNED NOT NULL DEFAULT 0,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (business_id, file_resource_id),
    KEY idx_business_file_sort (business_id, sort_no, file_resource_id),
    CONSTRAINT fk_business_file_business FOREIGN KEY (business_id) REFERENCES business (id),
    CONSTRAINT fk_business_file_resource FOREIGN KEY (file_resource_id) REFERENCES file_resource (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE business_travel_car (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    business_id BIGINT UNSIGNED NOT NULL,
    model VARCHAR(128) NOT NULL,
    seat_num INT UNSIGNED NOT NULL,
    description VARCHAR(500) NULL,
    image_resource_id BIGINT UNSIGNED NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'ENABLED',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    deleted_at DATETIME(3) NULL,
    PRIMARY KEY (id),
    KEY idx_travel_car_business_status (business_id, status, deleted_at),
    KEY idx_travel_car_image (image_resource_id),
    CONSTRAINT fk_travel_car_business FOREIGN KEY (business_id) REFERENCES business (id),
    CONSTRAINT fk_travel_car_image FOREIGN KEY (image_resource_id) REFERENCES file_resource (id),
    CONSTRAINT ck_travel_car_seat_num CHECK (seat_num > 0),
    CONSTRAINT ck_travel_car_status CHECK (status IN ('ENABLED', 'DISABLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE business_food_dish (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    business_id BIGINT UNSIGNED NOT NULL,
    name VARCHAR(128) NOT NULL,
    description VARCHAR(500) NULL,
    image_resource_id BIGINT UNSIGNED NULL,
    sort_no INT UNSIGNED NOT NULL DEFAULT 0,
    status VARCHAR(16) NOT NULL DEFAULT 'ENABLED',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    deleted_at DATETIME(3) NULL,
    PRIMARY KEY (id),
    KEY idx_food_dish_business_status (business_id, status, deleted_at),
    KEY idx_food_dish_image (image_resource_id),
    CONSTRAINT fk_food_dish_business FOREIGN KEY (business_id) REFERENCES business (id),
    CONSTRAINT fk_food_dish_image FOREIGN KEY (image_resource_id) REFERENCES file_resource (id),
    CONSTRAINT ck_food_dish_status CHECK (status IN ('ENABLED', 'DISABLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE business_hotel_room (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    business_id BIGINT UNSIGNED NOT NULL,
    name VARCHAR(128) NOT NULL,
    bed_spec VARCHAR(128) NOT NULL,
    description VARCHAR(500) NULL,
    image_resource_id BIGINT UNSIGNED NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'ENABLED',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    deleted_at DATETIME(3) NULL,
    PRIMARY KEY (id),
    KEY idx_hotel_room_business_status (business_id, status, deleted_at),
    KEY idx_hotel_room_image (image_resource_id),
    CONSTRAINT fk_hotel_room_business FOREIGN KEY (business_id) REFERENCES business (id),
    CONSTRAINT fk_hotel_room_image FOREIGN KEY (image_resource_id) REFERENCES file_resource (id),
    CONSTRAINT ck_hotel_room_status CHECK (status IN ('ENABLED', 'DISABLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE reserve_order (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    order_no VARCHAR(32) NOT NULL,
    openid VARCHAR(128) NOT NULL COMMENT 'Frontend-provided identifier; not strong authentication',
    user_id BIGINT UNSIGNED NULL,
    business_id BIGINT UNSIGNED NOT NULL,
    business_name_snapshot VARCHAR(128) NOT NULL,
    business_type VARCHAR(16) NOT NULL,
    service_type VARCHAR(16) NOT NULL,
    contact_name VARCHAR(64) NOT NULL,
    contact_phone VARCHAR(32) NOT NULL,
    people_num INT UNSIGNED NOT NULL,
    service_date DATE NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    car_id BIGINT UNSIGNED NULL,
    car_spec_snapshot VARCHAR(255) NULL,
    car_quantity INT UNSIGNED NULL,
    service_mode VARCHAR(16) NULL,
    room_id BIGINT UNSIGNED NULL,
    room_spec_snapshot VARCHAR(255) NULL,
    room_quantity INT UNSIGNED NULL,
    meal_period VARCHAR(16) NULL,
    option_snapshot_json JSON NULL,
    extra_json JSON NULL,
    confirmed_at DATETIME(3) NULL,
    cancelled_at DATETIME(3) NULL,
    cancel_source VARCHAR(16) NULL,
    cancel_reason VARCHAR(500) NULL,
    version INT UNSIGNED NOT NULL DEFAULT 0,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_reserve_order_order_no (order_no),
    KEY idx_reserve_order_openid_created (openid, created_at, id),
    KEY idx_reserve_order_user_created (user_id, created_at, id),
    KEY idx_reserve_order_service_status_type (service_date, status, business_type),
    KEY idx_reserve_order_business_service (business_id, service_date, id),
    KEY idx_reserve_order_car (car_id),
    KEY idx_reserve_order_room (room_id),
    CONSTRAINT fk_reserve_order_user FOREIGN KEY (user_id) REFERENCES wx_user (id),
    CONSTRAINT fk_reserve_order_business FOREIGN KEY (business_id) REFERENCES business (id),
    CONSTRAINT fk_reserve_order_car FOREIGN KEY (car_id) REFERENCES business_travel_car (id),
    CONSTRAINT fk_reserve_order_room FOREIGN KEY (room_id) REFERENCES business_hotel_room (id),
    CONSTRAINT ck_reserve_order_business_type CHECK (business_type IN ('TRAVEL', 'HOTEL', 'FOOD')),
    CONSTRAINT ck_reserve_order_service_type CHECK (service_type IN ('TRAVEL', 'HOTEL', 'FOOD')),
    CONSTRAINT ck_reserve_order_type_match CHECK (business_type = service_type),
    CONSTRAINT ck_reserve_order_status CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED')),
    CONSTRAINT ck_reserve_order_people CHECK (people_num > 0),
    CONSTRAINT ck_reserve_order_service_mode CHECK (service_mode IS NULL OR service_mode IN ('DAY_CHARTER', 'ROUND_TRIP')),
    CONSTRAINT ck_reserve_order_meal_period CHECK (meal_period IS NULL OR meal_period IN ('BREAKFAST', 'LUNCH', 'DINNER')),
    CONSTRAINT ck_reserve_order_car_quantity CHECK (car_quantity IS NULL OR car_quantity > 0),
    CONSTRAINT ck_reserve_order_room_quantity CHECK (room_quantity IS NULL OR room_quantity > 0),
    CONSTRAINT ck_reserve_order_type_fields CHECK (
        (service_type = 'TRAVEL' AND car_id IS NOT NULL AND car_spec_snapshot IS NOT NULL AND car_quantity IS NOT NULL AND service_mode IS NOT NULL
            AND room_id IS NULL AND room_spec_snapshot IS NULL AND room_quantity IS NULL AND meal_period IS NULL)
        OR (service_type = 'HOTEL' AND room_id IS NOT NULL AND room_spec_snapshot IS NOT NULL AND room_quantity IS NOT NULL
            AND car_id IS NULL AND car_spec_snapshot IS NULL AND car_quantity IS NULL AND service_mode IS NULL AND meal_period IS NULL)
        OR (service_type = 'FOOD' AND meal_period IS NOT NULL
            AND car_id IS NULL AND car_spec_snapshot IS NULL AND car_quantity IS NULL AND service_mode IS NULL
            AND room_id IS NULL AND room_spec_snapshot IS NULL AND room_quantity IS NULL)
    ),
    CONSTRAINT ck_reserve_order_cancel_fields CHECK (
        (status = 'CANCELLED' AND cancelled_at IS NOT NULL AND cancel_source IN ('USER', 'ADMIN', 'MERCHANT')
            AND (cancel_source = 'USER' OR cancel_reason IS NOT NULL))
        OR (status <> 'CANCELLED' AND cancelled_at IS NULL AND cancel_source IS NULL AND cancel_reason IS NULL)
    ),
    CONSTRAINT ck_reserve_order_confirmed_at CHECK (
        (status = 'CONFIRMED' AND confirmed_at IS NOT NULL)
        OR (status <> 'CONFIRMED' AND confirmed_at IS NULL)
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE order_status_log (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    order_id BIGINT UNSIGNED NOT NULL,
    from_status VARCHAR(16) NULL,
    to_status VARCHAR(16) NOT NULL,
    operator_type VARCHAR(16) NOT NULL,
    operator_id BIGINT UNSIGNED NULL,
    reason VARCHAR(500) NULL,
    request_id VARCHAR(64) NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_order_status_log_order_created (order_id, created_at, id),
    CONSTRAINT fk_order_status_log_order FOREIGN KEY (order_id) REFERENCES reserve_order (id),
    CONSTRAINT ck_order_status_log_from CHECK (from_status IS NULL OR from_status IN ('PENDING', 'CONFIRMED', 'CANCELLED')),
    CONSTRAINT ck_order_status_log_to CHECK (to_status IN ('PENDING', 'CONFIRMED', 'CANCELLED')),
    CONSTRAINT ck_order_status_log_operator CHECK (operator_type IN ('USER', 'ADMIN', 'MERCHANT', 'SYSTEM')),
    CONSTRAINT ck_order_status_log_transition CHECK (
        (from_status IS NULL AND to_status = 'PENDING')
        OR (from_status = 'PENDING' AND to_status IN ('CONFIRMED', 'CANCELLED'))
    ),
    CONSTRAINT ck_order_status_log_cancel_reason CHECK (
        to_status <> 'CANCELLED' OR operator_type = 'USER' OR reason IS NOT NULL
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE api_idempotency (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    openid VARCHAR(128) NOT NULL,
    idempotency_key VARCHAR(128) NOT NULL,
    request_hash CHAR(64) NOT NULL,
    response_ref VARCHAR(128) NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'PROCESSING',
    expires_at DATETIME(3) NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_api_idempotency_openid_key (openid, idempotency_key),
    KEY idx_api_idempotency_expires (expires_at),
    CONSTRAINT ck_api_idempotency_status CHECK (status IN ('PROCESSING', 'SUCCEEDED', 'FAILED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
