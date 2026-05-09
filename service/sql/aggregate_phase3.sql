-- 可信对象平台第三阶段增量脚本
-- 模板引擎字段增强 + 事件快照增强 + 标签绑定对象语义增强

ALTER TABLE `aggregate_subject_field`
  ADD COLUMN `validation_rule` varchar(500) DEFAULT NULL COMMENT '校验规则' AFTER `options_json`,
  ADD COLUMN `indexed_flag` char(1) DEFAULT '0' COMMENT '是否参与检索（0否 1是）' AFTER `required_flag`,
  ADD COLUMN `trace_flag` char(1) DEFAULT '0' COMMENT '是否参与追溯（0否 1是）' AFTER `indexed_flag`,
  ADD COLUMN `search_flag` char(1) DEFAULT '0' COMMENT '是否参与导出检索（0否 1是）' AFTER `trace_flag`,
  ADD COLUMN `export_flag` char(1) DEFAULT '0' COMMENT '是否参与导出（0否 1是）' AFTER `search_flag`,
  ADD COLUMN `editable_event_types` varchar(255) DEFAULT NULL COMMENT '可编辑事件节点，逗号分隔' AFTER `write_scope`;

ALTER TABLE `aggregate_event`
  ADD COLUMN `object_id` bigint(20) DEFAULT NULL COMMENT '可信对象ID' AFTER `material_id`,
  ADD COLUMN `source_module` varchar(64) DEFAULT NULL COMMENT '来源模块' AFTER `location_name`,
  ADD COLUMN `snapshot_data` longtext COMMENT '事件快照数据' AFTER `raw_payload`;

ALTER TABLE `aggregate_rfid_identity`
  ADD COLUMN `bind_object_type` varchar(64) DEFAULT NULL COMMENT '绑定对象类型' AFTER `bind_goods_name`;

ALTER TABLE `aggregate_subject_bind_record`
  ADD COLUMN `object_id` bigint(20) DEFAULT NULL COMMENT '可信对象ID' AFTER `rfid_code`,
  ADD COLUMN `object_code` varchar(64) DEFAULT NULL COMMENT '可信对象编码' AFTER `object_id`,
  ADD COLUMN `object_name` varchar(128) DEFAULT NULL COMMENT '可信对象名称' AFTER `object_code`;

UPDATE `aggregate_subject_field`
SET `trace_flag` = '1',
    `export_flag` = '1'
WHERE `required_flag` = '1';

