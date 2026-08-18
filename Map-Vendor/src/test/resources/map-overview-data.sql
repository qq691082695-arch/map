INSERT INTO university VALUES
 (2, '启用高校', '高校简介', '[{"latitude":31.1,"longitude":121.1},{"latitude":31.2,"longitude":121.2},{"latitude":31.3,"longitude":121.3}]', 'ENABLED', NULL),
 (3, '禁用高校', NULL, '[{"latitude":30,"longitude":120},{"latitude":31,"longitude":121},{"latitude":32,"longitude":122}]', 'DISABLED', NULL),
 (4, '删除高校', NULL, '[{"latitude":30,"longitude":120},{"latitude":31,"longitude":121},{"latitude":32,"longitude":122}]', 'ENABLED', CURRENT_TIMESTAMP);

INSERT INTO business VALUES
 (10, '出行商家', '地址甲', 121.1000000, 31.1000000, 'TRAVEL', '出行简介', 'ENABLED', NULL),
 (11, '住宿商家', '地址乙', 121.2000000, 31.2000000, 'HOTEL', NULL, 'ENABLED', NULL),
 (12, '禁用餐饮', '地址丙', 121.3000000, 31.3000000, 'FOOD', NULL, 'DISABLED', NULL),
 (13, '删除餐饮', '地址丁', 121.4000000, 31.4000000, 'FOOD', NULL, 'ENABLED', CURRENT_TIMESTAMP),
 (14, '餐饮商家', '地址戊', 121.5000000, 31.5000000, 'FOOD', NULL, 'ENABLED', NULL);

INSERT INTO file_resource VALUES
 (100, '/images/second.png', 'ACTIVE', NULL),
 (101, '/images/first.png', 'ACTIVE', NULL),
 (102, '/images/disabled.png', 'DISABLED', NULL),
 (103, '/images/deleted.png', 'ACTIVE', CURRENT_TIMESTAMP);

INSERT INTO university_file_relation VALUES (2, 100, 2), (2, 101, 1), (2, 102, 0);
INSERT INTO business_file_relation VALUES (10, 100, 2), (10, 101, 1), (11, 102, 0), (14, 103, 0);
