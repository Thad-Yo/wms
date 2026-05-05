-- 数字骨料管理平台第一阶段增量脚本
-- RFID身份建档 -> 入库事件 -> 出库事件 -> 移动事件 -> 时间线查询

DROP TABLE IF EXISTS `aggregate_material`;
CREATE TABLE `aggregate_material` (
  `material_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '骨料档案ID',
  `material_code` varchar(64) NOT NULL COMMENT '骨料编号',
  `material_name` varchar(128) NOT NULL COMMENT '骨料名称',
  `material_type` varchar(64) DEFAULT NULL COMMENT '骨料类型',
  `specification` varchar(128) DEFAULT NULL COMMENT '规格/粒径',
  `origin_place` varchar(255) DEFAULT NULL COMMENT '产地',
  `batch_no` varchar(64) DEFAULT NULL COMMENT '批次号',
  `supplier_id` bigint(20) DEFAULT NULL COMMENT '供应商ID',
  `supplier_name` varchar(128) DEFAULT NULL COMMENT '供应商名称',
  `unit` varchar(32) DEFAULT '吨' COMMENT '计量单位',
  `quality_grade` varchar(64) DEFAULT NULL COMMENT '质量/强度等级',
  `owner_user_id` bigint(20) DEFAULT NULL COMMENT '分配用户ID',
  `owner_user_name` varchar(64) DEFAULT NULL COMMENT '分配用户',
  `rfid_count` bigint(20) DEFAULT '0' COMMENT 'RFID数量',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`material_id`),
  KEY `idx_aggregate_material_code` (`material_code`),
  KEY `idx_aggregate_material_batch` (`batch_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数字骨料基础档案';

DROP TABLE IF EXISTS `aggregate_rfid_identity`;
CREATE TABLE `aggregate_rfid_identity` (
  `identity_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'RFID身份ID',
  `rfid_code` varchar(128) NOT NULL COMMENT 'RFID/EPC编码',
  `tid_code` varchar(128) DEFAULT NULL COMMENT 'TID编码',
  `material_id` bigint(20) NOT NULL COMMENT '骨料档案ID',
  `material_code` varchar(64) DEFAULT NULL COMMENT '骨料编号',
  `material_name` varchar(128) DEFAULT NULL COMMENT '骨料名称',
  `batch_no` varchar(64) DEFAULT NULL COMMENT '批次号',
  `identity_level` varchar(32) DEFAULT 'BATCH' COMMENT '身份粒度：BATCH批次/BAG吨包/VEHICLE车辆批次/UNIT单体',
  `current_state` varchar(32) DEFAULT 'CREATED' COMMENT '当前状态',
  `current_warehouse_id` bigint(20) DEFAULT NULL COMMENT '当前仓库ID',
  `current_warehouse_name` varchar(128) DEFAULT NULL COMMENT '当前仓库名称',
  `owner_user_id` bigint(20) DEFAULT NULL COMMENT '分配用户ID',
  `owner_user_name` varchar(64) DEFAULT NULL COMMENT '分配用户',
  `bind_goods_id` bigint(20) DEFAULT NULL COMMENT '绑定货品ID',
  `bind_goods_code` varchar(64) DEFAULT NULL COMMENT '绑定货品编号',
  `bind_goods_name` varchar(128) DEFAULT NULL COMMENT '绑定货品名称',
  `bind_goods_time` datetime DEFAULT NULL COMMENT '绑定货品时间',
  `current_location` varchar(255) DEFAULT NULL COMMENT '当前位置',
  `bind_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
  `last_event_time` datetime DEFAULT NULL COMMENT '最近事件时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`identity_id`),
  UNIQUE KEY `uk_aggregate_rfid_code` (`rfid_code`),
  KEY `idx_aggregate_rfid_material` (`material_id`),
  KEY `idx_aggregate_rfid_state` (`current_state`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数字骨料RFID身份';

DROP TABLE IF EXISTS `aggregate_event`;
CREATE TABLE `aggregate_event` (
  `event_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '事件ID',
  `identity_id` bigint(20) NOT NULL COMMENT 'RFID身份ID',
  `rfid_code` varchar(128) NOT NULL COMMENT 'RFID/EPC编码',
  `material_id` bigint(20) DEFAULT NULL COMMENT '骨料档案ID',
  `event_type` varchar(32) NOT NULL COMMENT '事件类型',
  `event_name` varchar(64) DEFAULT NULL COMMENT '事件名称',
  `event_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '事件时间',
  `location_name` varchar(255) DEFAULT NULL COMMENT '地点名称',
  `longitude` decimal(12,8) DEFAULT NULL COMMENT '经度',
  `latitude` decimal(12,8) DEFAULT NULL COMMENT '纬度',
  `operator_id` bigint(20) DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(64) DEFAULT NULL COMMENT '操作人',
  `action_name` varchar(128) DEFAULT NULL COMMENT '操作行为',
  `device_id` bigint(20) DEFAULT NULL COMMENT '设备ID',
  `device_code` varchar(64) DEFAULT NULL COMMENT '设备编号',
  `device_type` varchar(32) DEFAULT NULL COMMENT '设备类型',
  `warehouse_id` bigint(20) DEFAULT NULL COMMENT '仓库ID',
  `warehouse_name` varchar(128) DEFAULT NULL COMMENT '仓库名称',
  `from_warehouse_id` bigint(20) DEFAULT NULL COMMENT '来源仓库ID',
  `from_warehouse_name` varchar(128) DEFAULT NULL COMMENT '来源仓库名称',
  `to_warehouse_id` bigint(20) DEFAULT NULL COMMENT '目标仓库ID',
  `to_warehouse_name` varchar(128) DEFAULT NULL COMMENT '目标仓库名称',
  `weight` decimal(12,3) DEFAULT NULL COMMENT '重量',
  `vehicle_no` varchar(32) DEFAULT NULL COMMENT '车牌号',
  `source_receipt_type` varchar(32) DEFAULT NULL COMMENT '来源单据类型',
  `source_receipt_id` bigint(20) DEFAULT NULL COMMENT '来源单据ID',
  `source_receipt_no` varchar(64) DEFAULT NULL COMMENT '来源单据编号',
  `raw_payload` text COMMENT '设备原始报文',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`event_id`),
  KEY `idx_aggregate_event_identity` (`identity_id`),
  KEY `idx_aggregate_event_rfid` (`rfid_code`),
  KEY `idx_aggregate_event_type_time` (`event_type`, `event_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数字骨料RFID事件流水';

DROP TABLE IF EXISTS `aggregate_device`;
CREATE TABLE `aggregate_device` (
  `device_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '设备ID',
  `device_code` varchar(64) NOT NULL COMMENT '设备编号',
  `device_name` varchar(128) NOT NULL COMMENT '设备名称',
  `device_type` varchar(32) NOT NULL COMMENT '设备类型：HANDHELD/WEIGHBRIDGE/GATE/READER',
  `location_name` varchar(255) DEFAULT NULL COMMENT '安装地点',
  `warehouse_id` bigint(20) DEFAULT NULL COMMENT '所属仓库ID',
  `warehouse_name` varchar(128) DEFAULT NULL COMMENT '所属仓库名称',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`device_id`),
  UNIQUE KEY `uk_aggregate_device_code` (`device_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数字骨料采集设备';

-- 菜单与权限增量。若菜单ID与现有系统冲突，可调整为未占用ID。
DELETE FROM `sys_menu` WHERE `menu_id` IN (2300, 2301, 2302, 2303, 2304, 2311, 2312, 2313, 2314, 2315, 2321, 2322, 2323, 2324, 2331, 2341);
INSERT INTO `sys_menu` VALUES (2300, '骨料管理', 0, 4, 'aggregate', NULL, '', 1, 0, 'M', '0', '0', '', 'tree', 'admin', NOW(), '', NULL, '数字骨料管理目录');
INSERT INTO `sys_menu` VALUES (2301, '骨料档案', 2300, 1, 'material', 'warehouse/aggregate/material/index', '', 1, 0, 'C', '0', '0', 'warehouse:aggregate:material:list', 'list', 'admin', NOW(), '', NULL, '数字骨料基础档案');
INSERT INTO `sys_menu` VALUES (2302, 'RFID身份', 2300, 2, 'rfid', 'warehouse/aggregate/rfid/index', '', 1, 0, 'C', '0', '0', 'warehouse:aggregate:rfid:list', 'component', 'admin', NOW(), '', NULL, '数字骨料RFID身份');
INSERT INTO `sys_menu` VALUES (2303, '事件采集', 2300, 3, 'event', 'warehouse/aggregate/event/index', '', 1, 0, 'C', '0', '0', 'warehouse:aggregate:event:list', 'form', 'admin', NOW(), '', NULL, '数字骨料事件采集');
INSERT INTO `sys_menu` VALUES (2304, '生命周期', 2300, 4, 'lifecycle', 'warehouse/aggregate/lifecycle/index', '', 1, 0, 'C', '0', '0', 'warehouse:aggregate:lifecycle:query', 'time-range', 'admin', NOW(), '', NULL, 'RFID生命周期时间线');

INSERT INTO `sys_menu` VALUES (2311, '骨料档案查询', 2301, 1, '', '', '', 1, 0, 'F', '0', '0', 'warehouse:aggregate:material:query', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (2312, '骨料档案新增', 2301, 2, '', '', '', 1, 0, 'F', '0', '0', 'warehouse:aggregate:material:add', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (2313, '骨料档案修改', 2301, 3, '', '', '', 1, 0, 'F', '0', '0', 'warehouse:aggregate:material:edit', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (2314, '骨料档案删除', 2301, 4, '', '', '', 1, 0, 'F', '0', '0', 'warehouse:aggregate:material:remove', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (2315, '骨料批量发行', 2301, 5, '', '', '', 1, 0, 'F', '0', '0', 'warehouse:aggregate:material:import', '#', 'admin', NOW(), '', NULL, '');

INSERT INTO `sys_menu` VALUES (2321, 'RFID身份查询', 2302, 1, '', '', '', 1, 0, 'F', '0', '0', 'warehouse:aggregate:rfid:query', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (2322, 'RFID身份新增', 2302, 2, '', '', '', 1, 0, 'F', '0', '0', 'warehouse:aggregate:rfid:add', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (2323, 'RFID身份修改', 2302, 3, '', '', '', 1, 0, 'F', '0', '0', 'warehouse:aggregate:rfid:edit', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (2324, 'RFID身份删除', 2302, 4, '', '', '', 1, 0, 'F', '0', '0', 'warehouse:aggregate:rfid:remove', '#', 'admin', NOW(), '', NULL, '');

INSERT INTO `sys_menu` VALUES (2331, '事件采集新增', 2303, 1, '', '', '', 1, 0, 'F', '0', '0', 'warehouse:aggregate:event:add', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (2341, '生命周期查询', 2304, 1, '', '', '', 1, 0, 'F', '0', '0', 'warehouse:aggregate:lifecycle:query', '#', 'admin', NOW(), '', NULL, '');
