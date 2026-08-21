DROP ALL OBJECTS;
CREATE TABLE business (
    id BIGINT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    address VARCHAR(255) NOT NULL,
    longitude DECIMAL(10,7) NOT NULL,
    latitude DECIMAL(10,7) NOT NULL,
    business_type VARCHAR(16) NOT NULL,
    intro VARCHAR(1000),
    food_contact_name VARCHAR(64),
    food_contact_phone VARCHAR(32),
    food_recommended_dishes VARCHAR(500),
    status VARCHAR(16) NOT NULL,
    deleted_at TIMESTAMP NULL
);

CREATE TABLE file_resource (
    id BIGINT PRIMARY KEY,
    public_url VARCHAR(1024) NOT NULL,
    status VARCHAR(16) NOT NULL,
    deleted_at TIMESTAMP NULL
);

CREATE TABLE business_file_relation (
    business_id BIGINT NOT NULL,
    file_resource_id BIGINT NOT NULL,
    sort_no INT NOT NULL,
    PRIMARY KEY (business_id, file_resource_id)
);

CREATE TABLE business_travel_car (
    id BIGINT PRIMARY KEY,
    business_id BIGINT NOT NULL,
    model VARCHAR(128) NOT NULL,
    seat_num INT NOT NULL,
    description VARCHAR(500),
    image_resource_id BIGINT,
    status VARCHAR(16) NOT NULL,
    deleted_at TIMESTAMP NULL
);

CREATE TABLE business_hotel_room (
    id BIGINT PRIMARY KEY,
    business_id BIGINT NOT NULL,
    name VARCHAR(128) NOT NULL,
    bed_spec VARCHAR(128) NOT NULL,
    description VARCHAR(500),
    image_resource_id BIGINT,
    status VARCHAR(16) NOT NULL,
    deleted_at TIMESTAMP NULL
);

CREATE TABLE business_food_dish (
    id BIGINT PRIMARY KEY,
    business_id BIGINT NOT NULL,
    name VARCHAR(128) NOT NULL,
    description VARCHAR(500),
    is_recommended BOOLEAN NOT NULL DEFAULT FALSE,
    image_resource_id BIGINT,
    sort_no INT NOT NULL,
    status VARCHAR(16) NOT NULL,
    deleted_at TIMESTAMP NULL
);
