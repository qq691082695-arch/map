DROP TABLE IF EXISTS university_file_relation;
DROP TABLE IF EXISTS file_resource;
DROP TABLE IF EXISTS university;

CREATE TABLE university (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    intro VARCHAR(2000),
    polygon_json VARCHAR(4000) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'ENABLED',
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP(3) NULL
);

CREATE TABLE file_resource (
    id BIGINT PRIMARY KEY,
    public_url VARCHAR(1024) NOT NULL,
    status VARCHAR(16) NOT NULL,
    deleted_at TIMESTAMP(3) NULL
);

CREATE TABLE university_file_relation (
    university_id BIGINT NOT NULL,
    file_resource_id BIGINT NOT NULL,
    sort_no INT NOT NULL,
    PRIMARY KEY (university_id, file_resource_id)
);
