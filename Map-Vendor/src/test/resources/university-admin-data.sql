INSERT INTO file_resource VALUES
 (100, '/images/one.png', 'ACTIVE', NULL),
 (101, '/images/two.png', 'ACTIVE', NULL),
 (102, '/images/disabled.png', 'DISABLED', NULL),
 (103, '/images/deleted.png', 'ACTIVE', CURRENT_TIMESTAMP);

INSERT INTO university (id, name, intro, polygon_json, status, created_at, updated_at, deleted_at) VALUES
 (10, 'Alpha University', 'intro', '[{"latitude":31.1,"longitude":121.1},{"latitude":31.2,"longitude":121.2},{"latitude":31.3,"longitude":121.3}]', 'ENABLED', '2026-01-01 00:00:00', '2026-01-01 00:00:00', NULL),
 (11, 'Beta University', NULL, '[{"latitude":30,"longitude":120},{"latitude":31,"longitude":121},{"latitude":32,"longitude":122}]', 'DISABLED', '2026-01-02 00:00:00', '2026-01-02 00:00:00', NULL),
 (12, 'Deleted University', NULL, '[{"latitude":30,"longitude":120},{"latitude":31,"longitude":121},{"latitude":32,"longitude":122}]', 'ENABLED', '2026-01-03 00:00:00', '2026-01-03 00:00:00', CURRENT_TIMESTAMP);

INSERT INTO university_file_relation VALUES (10, 100, 1), (10, 101, 0), (10, 102, 2);
