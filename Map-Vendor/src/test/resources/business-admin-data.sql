INSERT INTO business(id,name,address,longitude,latitude,business_type,intro,status,created_at,updated_at)
VALUES (10,'Travel Alpha','Road 1',121.1,31.1,'TRAVEL','travel','ENABLED','2026-08-17 10:00:00','2026-08-17 10:00:00'),
       (11,'Hotel Beta','Road 2',121.2,31.2,'HOTEL','hotel','DISABLED','2026-08-18 10:00:00','2026-08-18 10:00:00'),
       (12,'Deleted Food','Road 3',121.3,31.3,'FOOD','food','ENABLED','2026-08-19 10:00:00','2026-08-19 10:00:00');
UPDATE business SET deleted_at='2026-08-19 11:00:00' WHERE id=12;
INSERT INTO reserve_order(id,business_id,business_name_snapshot) VALUES(1000,10,'Travel Alpha');
