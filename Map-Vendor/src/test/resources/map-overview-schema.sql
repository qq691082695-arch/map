DROP TABLE IF EXISTS business_file_relation;
DROP TABLE IF EXISTS university_file_relation;
DROP TABLE IF EXISTS file_resource;
DROP TABLE IF EXISTS business;
DROP TABLE IF EXISTS university;

CREATE TABLE university (
    id BIGINT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    intro VARCHAR(1000),
    polygon_json VARCHAR(4000) NOT NULL,
    status VARCHAR(16) NOT NULL,
    deleted_at TIMESTAMP NULL
);

CREATE TABLE business (
    id BIGINT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    address VARCHAR(255) NOT NULL,
    longitude DECIMAL(10,7) NOT NULL,
    latitude DECIMAL(10,7) NOT NULL,
    business_type VARCHAR(16) NOT NULL,
    intro VARCHAR(1000),
    status VARCHAR(16) NOT NULL,
    deleted_at TIMESTAMP NULL
);

CREATE TABLE file_resource (
    id BIGINT PRIMARY KEY,
    public_url VARCHAR(1024) NOT NULL,
    status VARCHAR(16) NOT NULL,
    deleted_at TIMESTAMP NULL
);

CREATE TABLE university_file_relation (
    university_id BIGINT NOT NULL,
    file_resource_id BIGINT NOT NULL,
    sort_no INT NOT NULL,
    PRIMARY KEY (university_id, file_resource_id)
);

CREATE TABLE business_file_relation (
    business_id BIGINT NOT NULL,
    file_resource_id BIGINT NOT NULL,
    sort_no INT NOT NULL,
    PRIMARY KEY (business_id, file_resource_id)
);
