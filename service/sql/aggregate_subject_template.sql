-- 数字骨料主体模板配置增量脚本

DROP TABLE IF EXISTS `aggregate_subject_template`;
CREATE TABLE `aggregate_subject_template` (
  `template_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `subject_code` varchar(64) NOT NULL COMMENT '主体编码',
  `subject_name` varchar(128) NOT NULL COMMENT '主体名称',
  `module_name` varchar(128) DEFAULT NULL COMMENT '所属模块',
  `status` char(1) DEFAULT '1' COMMENT '状态（0使用中 1未使用）',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`template_id`),
  UNIQUE KEY `uk_aggregate_subject_code` (`subject_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数字骨料主体模板';

DROP TABLE IF EXISTS `aggregate_subject_field`;
CREATE TABLE `aggregate_subject_field` (
  `field_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '字段ID',
  `template_id` bigint(20) NOT NULL COMMENT '模板ID',
  `field_code` varchar(64) NOT NULL COMMENT '字段编码',
  `field_label` varchar(128) NOT NULL COMMENT '字段名称',
  `field_type` varchar(32) NOT NULL COMMENT '字段类型：input/textarea/select/date/number',
  `placeholder` varchar(255) DEFAULT NULL COMMENT '占位提示',
  `default_value` varchar(255) DEFAULT NULL COMMENT '默认值',
  `options_json` text COMMENT '下拉选项JSON',
  `validation_rule` varchar(500) DEFAULT NULL COMMENT '校验规则',
  `required_flag` char(1) DEFAULT '0' COMMENT '是否必填（0否 1是）',
  `indexed_flag` char(1) DEFAULT '0' COMMENT '是否参与检索（0否 1是）',
  `trace_flag` char(1) DEFAULT '0' COMMENT '是否参与追溯（0否 1是）',
  `search_flag` char(1) DEFAULT '0' COMMENT '是否参与导出检索（0否 1是）',
  `export_flag` char(1) DEFAULT '0' COMMENT '是否参与导出（0否 1是）',
  `write_scope` varchar(64) DEFAULT '1' COMMENT '允许写入次数，如1,2,3',
  `editable_event_types` varchar(255) DEFAULT NULL COMMENT '可编辑事件节点，逗号分隔',
  `sort_order` int(11) DEFAULT 1 COMMENT '排序',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`field_id`),
  KEY `idx_aggregate_subject_field_template` (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数字骨料主体模板字段';

DROP TABLE IF EXISTS `aggregate_subject_bind_record`;
CREATE TABLE `aggregate_subject_bind_record` (
  `record_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `identity_id` bigint(20) NOT NULL COMMENT 'RFID身份ID',
  `rfid_code` varchar(128) NOT NULL COMMENT 'RFID编码',
  `bind_goods_id` bigint(20) DEFAULT NULL COMMENT '绑定货品ID',
  `bind_goods_code` varchar(64) DEFAULT NULL COMMENT '绑定货品编码',
  `bind_goods_name` varchar(128) DEFAULT NULL COMMENT '绑定货品名称',
  `template_id` bigint(20) NOT NULL COMMENT '主体模板ID',
  `subject_code` varchar(64) DEFAULT NULL COMMENT '主体编码',
  `subject_name` varchar(128) DEFAULT NULL COMMENT '主体名称',
  `module_name` varchar(128) DEFAULT NULL COMMENT '所属模块',
  `write_no` int(11) DEFAULT 1 COMMENT '第几次写入',
  `form_data_json` longtext COMMENT '填写表单JSON',
  `field_snapshot_json` longtext COMMENT '字段快照JSON',
  `operator_id` bigint(20) DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(64) DEFAULT NULL COMMENT '操作人',
  `write_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '写入时间',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
  PRIMARY KEY (`record_id`),
  KEY `idx_aggregate_subject_bind_identity` (`identity_id`),
  KEY `idx_aggregate_subject_bind_template` (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数字骨料主体填写记录';

INSERT INTO `aggregate_subject_template`
(`template_id`, `subject_code`, `subject_name`, `module_name`, `status`, `create_by`, `create_time`, `remark`)
VALUES
(1, 'PRECAST', '水泥预制品模块', '水泥预制品模块', '0', 'admin', NOW(), '按图片内置，可直接使用'),
(2, 'LOGISTICS', '物流模块', '物流模块', '1', 'admin', NOW(), '按图片内置，可直接使用'),
(3, 'RETAIL', '一般商品销售', '一般商品销售', '1', 'admin', NOW(), '按图片内置，可直接使用');

INSERT INTO `aggregate_subject_field`
(`template_id`, `field_code`, `field_label`, `field_type`, `placeholder`, `required_flag`, `write_scope`, `sort_order`, `create_by`, `create_time`)
VALUES
(1, 'aggregate_serial_no', '骨料串号', 'input', '请输入骨料串号', '1', '', 1, 'admin', NOW()),
(1, 'product_model', '产品型号', 'input', '请输入产品型号', '1', '', 2, 'admin', NOW()),
(1, 'manufacturer', '生产厂家', 'input', '请输入生产厂家', '1', '', 3, 'admin', NOW()),
(1, 'production_team', '生产班组', 'input', '请输入生产班组', '0', '', 4, 'admin', NOW()),
(1, 'production_date', '生产日期', 'date', '请选择生产日期', '1', '', 5, 'admin', NOW()),
(1, 'raw_material', '原材料', 'textarea', '请输入原材料说明', '0', '', 6, 'admin', NOW()),
(1, 'production_other_info', '生产其它信息', 'textarea', '请输入生产其它信息', '0', '', 7, 'admin', NOW()),
(1, 'producer_writer', '生产端写入人', 'input', '请输入生产端写入人', '0', '', 8, 'admin', NOW()),
(1, 'project_name', '应用工程名称', 'input', '请输入应用工程名称', '0', '', 9, 'admin', NOW()),
(1, 'project_owner', '工程甲方', 'input', '请输入工程甲方', '0', '', 10, 'admin', NOW()),
(1, 'construction_party', '施工方', 'input', '请输入施工方', '0', '', 11, 'admin', NOW()),
(1, 'construction_time', '施工时间', 'date', '请选择施工时间', '0', '', 12, 'admin', NOW()),
(1, 'geo_position', '地理坐标', 'input', '请输入地理坐标', '0', '', 13, 'admin', NOW()),
(1, 'buried_depth', '预埋深度', 'number', '请输入预埋深度', '0', '', 14, 'admin', NOW()),
(1, 'constructor_writer', '施工方写入人', 'input', '请输入施工方写入人', '0', '', 15, 'admin', NOW()),
(2, 'aggregate_serial_no', '骨料串号', 'input', '请输入骨料串号', '1', '', 1, 'admin', NOW()),
(2, 'supplier_product_info', '供应商出库物品名称、型号和数量、保质期', 'textarea', '请输入供应商出库物品信息', '1', '', 2, 'admin', NOW()),
(2, 'manufacturer_info', '生产厂家信息', 'textarea', '请输入生产厂家信息', '0', '', 3, 'admin', NOW()),
(2, 'supplier_info', '供应商信息', 'textarea', '请输入供应商信息', '0', '', 4, 'admin', NOW()),
(2, 'supplier_out_time', '供应商出库时间', 'date', '请选择供应商出库时间', '0', '', 5, 'admin', NOW()),
(2, 'supplier_out_user', '供应商出库人', 'input', '请输入供应商出库人', '0', '', 6, 'admin', NOW()),
(2, 'receiver_product_info', '收货方入库物品名称、型号、数量、保质期', 'textarea', '请输入收货方入库物品信息', '0', '', 7, 'admin', NOW()),
(2, 'receiver_name', '收货人', 'input', '请输入收货人', '0', '', 8, 'admin', NOW()),
(3, 'digital_serial_no', '数字骨料串号', 'input', '请输入数字骨料串号', '1', '', 1, 'admin', NOW()),
(3, 'manufacturer_info', '生产厂家信息', 'textarea', '请输入生产厂家信息', '1', '', 2, 'admin', NOW()),
(3, 'product_model', '产品型号', 'input', '请输入产品型号', '1', '', 3, 'admin', NOW()),
(3, 'production_date', '生产日期', 'date', '请选择生产日期', '1', '', 4, 'admin', NOW()),
(3, 'quality_type', '合格品/样品/半成品', 'select', '请选择品质类型', '0', '', 5, 'admin', NOW()),
(3, 'local_batch_count', '本批次数量', 'number', '请输入本批次数量', '0', '', 6, 'admin', NOW()),
(3, 'dealer_info', '经销商信息', 'textarea', '请输入经销商信息', '0', '', 7, 'admin', NOW()),
(3, 'dealer_buy_time', '经销商购货时间', 'date', '请选择经销商购货时间', '0', '', 8, 'admin', NOW()),
(3, 'dealer_payment_status', '经销商付款状态', 'select', '请选择经销商付款状态', '0', '', 9, 'admin', NOW()),
(3, 'dealer_sales_scope', '经销商销售范围', 'textarea', '请输入经销商销售范围', '0', '', 10, 'admin', NOW()),
(3, 'user_geo_info', '用户经纬度信息', 'input', '请输入用户经纬度信息', '0', '', 11, 'admin', NOW()),
(3, 'user_info', '用户信息', 'textarea', '请输入用户信息', '0', '', 12, 'admin', NOW()),
(3, 'user_payment_status', '用户付款状态', 'select', '请选择用户付款状态', '0', '', 13, 'admin', NOW());

UPDATE `aggregate_subject_field` SET `options_json`='[{\"label\":\"合格品\",\"value\":\"合格品\"},{\"label\":\"样品\",\"value\":\"样品\"},{\"label\":\"半成品\",\"value\":\"半成品\"}]' WHERE `template_id`=3 AND `field_code`='quality_type';
UPDATE `aggregate_subject_field` SET `options_json`='[{\"label\":\"已付款\",\"value\":\"已付款\"},{\"label\":\"未付款\",\"value\":\"未付款\"},{\"label\":\"部分付款\",\"value\":\"部分付款\"}]' WHERE `template_id`=3 AND `field_code` IN ('dealer_payment_status', 'user_payment_status');
