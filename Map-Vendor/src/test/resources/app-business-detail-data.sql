INSERT INTO business VALUES
 (10, '出行商家', '地址甲', 121.1000000, 31.1000000, 'TRAVEL', '出行简介', NULL, NULL, NULL, 'ENABLED', NULL),
 (11, '住宿商家', '地址乙', 121.2000000, 31.2000000, 'HOTEL', NULL, NULL, NULL, NULL, 'ENABLED', NULL),
 (12, '餐饮商家', '地址丙', 121.3000000, 31.3000000, 'FOOD', '餐饮简介', '李经理', '027-88888888', '推荐菜一、推荐菜二', 'ENABLED', NULL),
 (13, '禁用商家', '地址丁', 121.4000000, 31.4000000, 'TRAVEL', NULL, NULL, NULL, NULL, 'DISABLED', NULL),
 (14, '删除商家', '地址戊', 121.5000000, 31.5000000, 'TRAVEL', NULL, NULL, NULL, NULL, 'ENABLED', CURRENT_TIMESTAMP);

INSERT INTO file_resource VALUES
 (100, '/images/active.png', 'ACTIVE', NULL),
 (101, '/images/disabled.png', 'DISABLED', NULL),
 (102, '/images/deleted.png', 'ACTIVE', CURRENT_TIMESTAMP);

INSERT INTO business_file_relation VALUES
 (10, 100, 1), (10, 102, 0);

INSERT INTO business_travel_car VALUES
 (101, 10, '别克GL8', 7, '商务接送', 100, 'ENABLED', NULL),
 (102, 10, '禁用车辆', 40, NULL, NULL, 'DISABLED', NULL),
 (103, 10, '删除车辆', 8, NULL, NULL, 'ENABLED', CURRENT_TIMESTAMP),
 (104, 40, '他人车辆', 5, NULL, NULL, 'ENABLED', NULL);

INSERT INTO business_hotel_room VALUES
 (201, 11, '商务套房', '1.8m大床', '含会客区', 100, 'ENABLED', NULL),
 (202, 11, '禁用房型', '1.2m', NULL, NULL, 'DISABLED', NULL),
 (203, 99, '他人房型', '2m', NULL, NULL, 'ENABLED', NULL);

INSERT INTO business_food_dish VALUES
 (301, 12, '推荐菜一', '第一道菜', TRUE, 100, 2, 'ENABLED', NULL),
 (302, 12, '推荐菜二', '第二道菜', TRUE, 101, 1, 'ENABLED', NULL),
 (303, 12, '禁用菜品', NULL, FALSE, NULL, 0, 'DISABLED', NULL);
