-- 数字骨料管理平台第二阶段增量脚本
-- 批次展示、RFID批量发行、下游用户只读分配数据

ALTER TABLE `aggregate_material`
  ADD COLUMN `owner_user_id` bigint(20) DEFAULT NULL COMMENT '分配用户ID' AFTER `quality_grade`,
  ADD COLUMN `owner_user_name` varchar(64) DEFAULT NULL COMMENT '分配用户' AFTER `owner_user_id`,
  ADD COLUMN `rfid_count` bigint(20) DEFAULT '0' COMMENT 'RFID数量' AFTER `owner_user_name`;

ALTER TABLE `aggregate_rfid_identity`
  ADD COLUMN `owner_user_id` bigint(20) DEFAULT NULL COMMENT '分配用户ID' AFTER `current_warehouse_name`,
  ADD COLUMN `owner_user_name` varchar(64) DEFAULT NULL COMMENT '分配用户' AFTER `owner_user_id`;

ALTER TABLE `aggregate_rfid_identity`
  ADD COLUMN `bind_goods_id` bigint(20) DEFAULT NULL COMMENT '绑定货品ID' AFTER `owner_user_name`,
  ADD COLUMN `bind_goods_code` varchar(64) DEFAULT NULL COMMENT '绑定货品编号' AFTER `bind_goods_id`,
  ADD COLUMN `bind_goods_name` varchar(128) DEFAULT NULL COMMENT '绑定货品名称' AFTER `bind_goods_code`,
  ADD COLUMN `bind_goods_time` datetime DEFAULT NULL COMMENT '绑定货品时间' AFTER `bind_goods_name`;

UPDATE aggregate_material m
SET rfid_count = (
  SELECT COUNT(1)
  FROM aggregate_rfid_identity r
  WHERE r.material_id = m.material_id AND r.del_flag = '0'
)
WHERE m.rfid_count IS NULL OR m.rfid_count = 0;

UPDATE aggregate_rfid_identity r
JOIN aggregate_material m ON r.material_id = m.material_id
SET r.owner_user_id = m.owner_user_id,
    r.owner_user_name = m.owner_user_name
WHERE r.owner_user_id IS NULL;

DELETE FROM `sys_menu` WHERE `menu_id` = 2315;
INSERT INTO `sys_menu` VALUES (2315, '骨料批量发行', 2301, 5, '', '', '', 1, 0, 'F', '0', '0', 'warehouse:aggregate:material:import', '#', 'admin', NOW(), '', NULL, '');
