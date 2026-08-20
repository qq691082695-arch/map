INSERT INTO business VALUES(10,'TRAVEL',NULL),(20,'HOTEL',NULL),(30,'FOOD',NULL),(40,'TRAVEL',NULL),(50,'TRAVEL',CURRENT_TIMESTAMP);
INSERT INTO file_resource VALUES(1,'/api/v1/files/demo-car.jpg',NULL);
INSERT INTO business_travel_car(id,business_id,model,seat_num,image_resource_id,status) VALUES(101,10,'Bus',40,1,'ENABLED'),(102,40,'Van',8,NULL,'ENABLED');
INSERT INTO business_hotel_room(id,business_id,name,bed_spec,status) VALUES(201,20,'Suite','King','ENABLED');
INSERT INTO business_food_dish(id,business_id,name,sort_no,status) VALUES(301,30,'Noodles',1,'ENABLED');
