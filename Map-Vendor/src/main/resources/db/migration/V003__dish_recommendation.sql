ALTER TABLE business_food_dish
    ADD COLUMN is_recommended TINYINT(1) NOT NULL DEFAULT 0 AFTER description,
    ADD KEY idx_food_dish_recommended (business_id, is_recommended, status, deleted_at);

-- 推荐菜改由菜品记录维护，旧的自由文本停止使用但暂时保留列以兼容回滚。
UPDATE business SET food_recommended_dishes = NULL WHERE food_recommended_dishes IS NOT NULL;
