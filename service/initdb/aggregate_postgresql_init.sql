-- Bone trace platform PostgreSQL init script
-- Includes:
-- 1. New canonical schema for the repaired product model
--    bone_rfid / object / template / template_field / object_event
-- 2. Legacy aggregate_* compatibility schema used by current backend
-- 3. MySQL compatibility helpers used by the existing backend

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE OR REPLACE FUNCTION sysdate()
RETURNS TIMESTAMP AS $$
BEGIN
  RETURN CURRENT_TIMESTAMP;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION ifnull(TEXT, TEXT)
RETURNS TEXT AS $$
BEGIN
  RETURN COALESCE($1, $2);
END;
$$ LANGUAGE plpgsql IMMUTABLE;

CREATE OR REPLACE FUNCTION ifnull(VARCHAR, VARCHAR)
RETURNS VARCHAR AS $$
BEGIN
  RETURN COALESCE($1, $2);
END;
$$ LANGUAGE plpgsql IMMUTABLE;

CREATE OR REPLACE FUNCTION find_in_set(needle TEXT, haystack TEXT)
RETURNS INTEGER AS $$
DECLARE
  items TEXT[];
  pos INTEGER;
BEGIN
  IF needle IS NULL OR haystack IS NULL OR haystack = '' THEN
    RETURN 0;
  END IF;
  items := string_to_array(haystack, ',');
  pos := array_position(items, needle);
  RETURN COALESCE(pos, 0);
END;
$$ LANGUAGE plpgsql IMMUTABLE;

CREATE OR REPLACE FUNCTION date_format(ts TIMESTAMP, fmt TEXT)
RETURNS TEXT AS $$
BEGIN
  IF ts IS NULL THEN
    RETURN NULL;
  END IF;
  IF fmt = '%y%m%d' THEN
    RETURN to_char(ts, 'YYMMDD');
  ELSIF fmt = '%Y-%m-%d' THEN
    RETURN to_char(ts, 'YYYY-MM-DD');
  ELSIF fmt = '%Y-%m-%d %H:%i:%s' THEN
    RETURN to_char(ts, 'YYYY-MM-DD HH24:MI:SS');
  END IF;
  RETURN to_char(ts, 'YYYY-MM-DD HH24:MI:SS');
END;
$$ LANGUAGE plpgsql IMMUTABLE;

CREATE OR REPLACE FUNCTION timestampdiff(unit_name TEXT, start_time TIMESTAMP, end_time TIMESTAMP)
RETURNS BIGINT AS $$
DECLARE
  diff_seconds NUMERIC;
BEGIN
  IF start_time IS NULL OR end_time IS NULL THEN
    RETURN NULL;
  END IF;
  diff_seconds := EXTRACT(EPOCH FROM (end_time - start_time));
  CASE UPPER(unit_name)
    WHEN 'SECOND' THEN RETURN diff_seconds::BIGINT;
    WHEN 'MINUTE' THEN RETURN FLOOR(diff_seconds / 60)::BIGINT;
    WHEN 'HOUR' THEN RETURN FLOOR(diff_seconds / 3600)::BIGINT;
    WHEN 'DAY' THEN RETURN FLOOR(diff_seconds / 86400)::BIGINT;
    ELSE RETURN diff_seconds::BIGINT;
  END CASE;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

DROP VIEW IF EXISTS dual;
CREATE VIEW dual AS SELECT 1 AS dummy;

DROP TABLE IF EXISTS object_event CASCADE;
DROP TABLE IF EXISTS template_field CASCADE;
DROP TABLE IF EXISTS template CASCADE;
DROP TABLE IF EXISTS "object" CASCADE;
DROP TABLE IF EXISTS bone_rfid CASCADE;

CREATE TABLE bone_rfid (
  bone_rfid_id BIGSERIAL PRIMARY KEY,
  bone_rfid_code VARCHAR(128) NOT NULL UNIQUE,
  tid_code VARCHAR(128),
  bone_code VARCHAR(64) NOT NULL UNIQUE,
  bone_name VARCHAR(128) NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'UNASSIGNED',
  current_object_id BIGINT,
  current_object_code VARCHAR(64),
  current_object_name VARCHAR(128),
  allocated_by BIGINT,
  allocated_name VARCHAR(64),
  allocated_time TIMESTAMP,
  activated_time TIMESTAMP,
  unbind_time TIMESTAMP,
  completed_time TIMESTAMP,
  scrap_time TIMESTAMP,
  last_event_id BIGINT,
  last_event_time TIMESTAMP,
  current_location VARCHAR(255),
  current_warehouse_id BIGINT,
  current_warehouse_name VARCHAR(128),
  extra_data JSONB DEFAULT '{}'::jsonb,
  del_flag CHAR(1) NOT NULL DEFAULT '0',
  create_by VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_by VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  remark VARCHAR(500)
);
CREATE INDEX idx_bone_rfid_status ON bone_rfid(status);
CREATE INDEX idx_bone_rfid_object ON bone_rfid(current_object_id);
CREATE INDEX idx_bone_rfid_last_event_time ON bone_rfid(last_event_time);
CREATE INDEX idx_bone_rfid_extra_data_gin ON bone_rfid USING GIN(extra_data);
CREATE UNIQUE INDEX uk_bone_rfid_current_object_active
  ON bone_rfid(current_object_id)
  WHERE current_object_id IS NOT NULL AND del_flag = '0' AND status IN ('ALLOCATED', 'IN_USE');

CREATE TABLE "object" (
  object_id BIGSERIAL PRIMARY KEY,
  object_code VARCHAR(64) NOT NULL UNIQUE,
  object_name VARCHAR(128) NOT NULL,
  object_type VARCHAR(64) NOT NULL,
  template_id BIGINT,
  template_code VARCHAR(64),
  template_name VARCHAR(128),
  bone_rfid_id BIGINT,
  bone_rfid_code VARCHAR(128),
  bind_status VARCHAR(32) NOT NULL DEFAULT 'UNBOUND',
  lifecycle_status VARCHAR(32) NOT NULL DEFAULT 'CREATED',
  owner_user_id BIGINT,
  owner_user_name VARCHAR(64),
  source_system VARCHAR(64),
  search_text TEXT,
  fixed_data JSONB NOT NULL DEFAULT '{}'::jsonb,
  dynamic_data JSONB NOT NULL DEFAULT '{}'::jsonb,
  trace_data JSONB NOT NULL DEFAULT '{}'::jsonb,
  ext_data JSONB NOT NULL DEFAULT '{}'::jsonb,
  bind_time TIMESTAMP,
  unbind_time TIMESTAMP,
  completed_time TIMESTAMP,
  del_flag CHAR(1) NOT NULL DEFAULT '0',
  create_by VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_by VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  remark VARCHAR(500)
);
CREATE INDEX idx_object_type ON "object"(object_type);
CREATE INDEX idx_object_template ON "object"(template_id);
CREATE INDEX idx_object_bind_status ON "object"(bind_status);
CREATE INDEX idx_object_lifecycle_status ON "object"(lifecycle_status);
CREATE INDEX idx_object_owner ON "object"(owner_user_id);
CREATE INDEX idx_object_dynamic_data_gin ON "object" USING GIN(dynamic_data);
CREATE INDEX idx_object_trace_data_gin ON "object" USING GIN(trace_data);
CREATE INDEX idx_object_fixed_data_gin ON "object" USING GIN(fixed_data);
CREATE UNIQUE INDEX uk_object_bone_rfid_active
  ON "object"(bone_rfid_id)
  WHERE bone_rfid_id IS NOT NULL AND del_flag = '0' AND bind_status = 'BOUND';

CREATE TABLE template (
  template_id BIGSERIAL PRIMARY KEY,
  template_code VARCHAR(64) NOT NULL UNIQUE,
  template_name VARCHAR(128) NOT NULL,
  object_type VARCHAR(64) NOT NULL,
  version_no INT NOT NULL DEFAULT 1,
  status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
  active_flag CHAR(1) NOT NULL DEFAULT '0',
  description VARCHAR(500),
  schema_json JSONB NOT NULL DEFAULT '{}'::jsonb,
  permission_json JSONB NOT NULL DEFAULT '{}'::jsonb,
  del_flag CHAR(1) NOT NULL DEFAULT '0',
  create_by VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_by VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  remark VARCHAR(500)
);
CREATE INDEX idx_template_object_type ON template(object_type);
CREATE INDEX idx_template_status ON template(status);
CREATE INDEX idx_template_schema_gin ON template USING GIN(schema_json);
CREATE UNIQUE INDEX uk_template_single_active_per_object_type
  ON template(object_type)
  WHERE active_flag = '1' AND del_flag = '0';

CREATE TABLE template_field (
  field_id BIGSERIAL PRIMARY KEY,
  template_id BIGINT NOT NULL,
  field_code VARCHAR(64) NOT NULL,
  field_label VARCHAR(128) NOT NULL,
  field_type VARCHAR(32) NOT NULL,
  required_flag CHAR(1) NOT NULL DEFAULT '0',
  default_value TEXT,
  options JSONB NOT NULL DEFAULT '[]'::jsonb,
  validation_rule JSONB NOT NULL DEFAULT '{}'::jsonb,
  indexed_flag CHAR(1) NOT NULL DEFAULT '0',
  trace_flag CHAR(1) NOT NULL DEFAULT '0',
  search_flag CHAR(1) NOT NULL DEFAULT '0',
  export_flag CHAR(1) NOT NULL DEFAULT '0',
  editable_flag CHAR(1) NOT NULL DEFAULT '1',
  editable_event_types JSONB NOT NULL DEFAULT '[]'::jsonb,
  permission_json JSONB NOT NULL DEFAULT '{}'::jsonb,
  sort_order INT NOT NULL DEFAULT 1,
  del_flag CHAR(1) NOT NULL DEFAULT '0',
  create_by VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_by VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  remark VARCHAR(500)
);
CREATE INDEX idx_template_field_template ON template_field(template_id);
CREATE INDEX idx_template_field_options_gin ON template_field USING GIN(options);
CREATE INDEX idx_template_field_validation_gin ON template_field USING GIN(validation_rule);
CREATE UNIQUE INDEX uk_template_field_code
  ON template_field(template_id, field_code)
  WHERE del_flag = '0';

CREATE TABLE object_event (
  event_id BIGSERIAL PRIMARY KEY,
  object_id BIGINT NOT NULL,
  object_code VARCHAR(64),
  object_name VARCHAR(128),
  bone_rfid_id BIGINT,
  bone_rfid_code VARCHAR(128),
  event_type VARCHAR(64) NOT NULL,
  event_name VARCHAR(128),
  event_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  operator_id BIGINT,
  operator_name VARCHAR(64),
  location VARCHAR(255),
  source_module VARCHAR(64),
  source_system VARCHAR(64),
  source_device_code VARCHAR(64),
  warehouse_id BIGINT,
  warehouse_name VARCHAR(128),
  snapshot_data JSONB NOT NULL DEFAULT '{}'::jsonb,
  before_data JSONB NOT NULL DEFAULT '{}'::jsonb,
  changed_fields JSONB NOT NULL DEFAULT '[]'::jsonb,
  ext_data JSONB NOT NULL DEFAULT '{}'::jsonb,
  prev_event_hash VARCHAR(128),
  event_hash VARCHAR(128),
  del_flag CHAR(1) NOT NULL DEFAULT '0',
  create_by VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  remark VARCHAR(500)
);
CREATE INDEX idx_object_event_object ON object_event(object_id, event_time DESC);
CREATE INDEX idx_object_event_bone_rfid ON object_event(bone_rfid_id, event_time DESC);
CREATE INDEX idx_object_event_type_time ON object_event(event_type, event_time DESC);
CREATE INDEX idx_object_event_snapshot_gin ON object_event USING GIN(snapshot_data);
CREATE INDEX idx_object_event_changed_fields_gin ON object_event USING GIN(changed_fields);

ALTER TABLE bone_rfid
  ADD CONSTRAINT chk_bone_rfid_status
  CHECK (status IN ('UNASSIGNED', 'ALLOCATED', 'IN_USE', 'COMPLETED', 'SCRAPPED'));

ALTER TABLE "object"
  ADD CONSTRAINT chk_object_bind_status
  CHECK (bind_status IN ('UNBOUND', 'BOUND', 'REBOUND'));

ALTER TABLE "object"
  ADD CONSTRAINT chk_object_lifecycle_status
  CHECK (lifecycle_status IN ('CREATED', 'BOUND', 'INBOUND', 'OUTBOUND', 'TRANSFER', 'SIGNED', 'INSTALLED', 'AFTER_SALE', 'DESTROYED'));

ALTER TABLE template
  ADD CONSTRAINT chk_template_status
  CHECK (status IN ('DRAFT', 'PUBLISHED', 'DISABLED'));

ALTER TABLE "object"
  ADD CONSTRAINT fk_object_template
  FOREIGN KEY (template_id) REFERENCES template(template_id);

ALTER TABLE "object"
  ADD CONSTRAINT fk_object_bone_rfid
  FOREIGN KEY (bone_rfid_id) REFERENCES bone_rfid(bone_rfid_id);

ALTER TABLE template_field
  ADD CONSTRAINT fk_template_field_template
  FOREIGN KEY (template_id) REFERENCES template(template_id);

ALTER TABLE object_event
  ADD CONSTRAINT fk_object_event_object
  FOREIGN KEY (object_id) REFERENCES "object"(object_id);

ALTER TABLE object_event
  ADD CONSTRAINT fk_object_event_bone_rfid
  FOREIGN KEY (bone_rfid_id) REFERENCES bone_rfid(bone_rfid_id);

CREATE OR REPLACE FUNCTION fill_object_event_hash()
RETURNS TRIGGER AS $$
DECLARE
  last_hash TEXT;
BEGIN
  IF NEW.prev_event_hash IS NULL OR NEW.prev_event_hash = '' THEN
    SELECT oe.event_hash
      INTO last_hash
      FROM object_event oe
     WHERE oe.object_id = NEW.object_id
     ORDER BY oe.event_time DESC, oe.event_id DESC
     LIMIT 1;
    NEW.prev_event_hash := last_hash;
  END IF;

  NEW.event_hash := encode(
    digest(
      concat_ws('|',
        coalesce(NEW.object_id::TEXT, ''),
        coalesce(NEW.object_code, ''),
        coalesce(NEW.bone_rfid_code, ''),
        coalesce(NEW.event_type, ''),
        coalesce(to_char(NEW.event_time, 'YYYY-MM-DD HH24:MI:SS'), ''),
        coalesce(NEW.operator_name, ''),
        coalesce(NEW.location, ''),
        coalesce(NEW.source_module, ''),
        coalesce(NEW.snapshot_data::TEXT, '{}'),
        coalesce(NEW.prev_event_hash, '')
      ),
      'sha256'
    ),
    'hex'
  );
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_fill_object_event_hash ON object_event;
CREATE TRIGGER trg_fill_object_event_hash
BEFORE INSERT ON object_event
FOR EACH ROW
EXECUTE FUNCTION fill_object_event_hash();

CREATE OR REPLACE VIEW bone_object_timeline AS
SELECT
  oe.event_id,
  oe.object_id,
  oe.object_code,
  oe.object_name,
  oe.bone_rfid_id,
  oe.bone_rfid_code,
  oe.event_type,
  oe.event_name,
  oe.event_time,
  oe.operator_id,
  oe.operator_name,
  oe.location,
  oe.source_module,
  oe.source_system,
  oe.source_device_code,
  oe.warehouse_id,
  oe.warehouse_name,
  oe.snapshot_data,
  oe.before_data,
  oe.changed_fields,
  oe.ext_data,
  oe.prev_event_hash,
  oe.event_hash,
  o.object_type,
  o.bind_status,
  o.lifecycle_status,
  br.status AS bone_status
FROM object_event oe
LEFT JOIN "object" o ON o.object_id = oe.object_id
LEFT JOIN bone_rfid br ON br.bone_rfid_id = oe.bone_rfid_id
WHERE oe.del_flag = '0';

COMMENT ON TABLE bone_rfid IS '骨料数字身份表，一对一绑定对象';
COMMENT ON TABLE "object" IS '对象信息表，动态属性使用JSONB存储';
COMMENT ON TABLE template IS '对象模板主表';
COMMENT ON TABLE template_field IS '对象模板字段定义表';
COMMENT ON TABLE object_event IS '对象事件中心，记录快照与哈希链';
COMMENT ON COLUMN bone_rfid.extra_data IS '骨料扩展属性JSONB';
COMMENT ON COLUMN "object".fixed_data IS '对象固定属性JSONB';
COMMENT ON COLUMN "object".dynamic_data IS '对象动态字段JSONB';
COMMENT ON COLUMN "object".trace_data IS '对象追溯重点字段JSONB';
COMMENT ON COLUMN template.schema_json IS '模板整体结构描述JSONB';
COMMENT ON COLUMN template.permission_json IS '模板权限定义JSONB';
COMMENT ON COLUMN template_field.options IS '下拉/枚举选项JSONB';
COMMENT ON COLUMN template_field.validation_rule IS '字段校验规则JSONB';
COMMENT ON COLUMN object_event.snapshot_data IS '事件发生时对象完整快照JSONB';
COMMENT ON COLUMN object_event.before_data IS '事件发生前对象快照JSONB';
COMMENT ON COLUMN object_event.changed_fields IS '本次事件变更字段列表JSONB';
COMMENT ON COLUMN object_event.prev_event_hash IS '前一事件哈希';
COMMENT ON COLUMN object_event.event_hash IS '当前事件哈希';
COMMENT ON VIEW bone_object_timeline IS '骨料对象追踪时间线视图，由事件中心实时聚合';

INSERT INTO template
(template_id, template_code, template_name, object_type, version_no, status, active_flag, description, create_by, create_time)
VALUES
(1, 'AGGREGATE_PRECAST', '骨料预制品模板', 'PRECAST', 1, 'PUBLISHED', '1', '预制品/工程场景骨料模板', 'admin', CURRENT_TIMESTAMP),
(2, 'AGGREGATE_LOGISTICS', '骨料物流模板', 'LOGISTICS', 1, 'PUBLISHED', '0', '物流交付场景骨料模板', 'admin', CURRENT_TIMESTAMP),
(3, 'AGGREGATE_RETAIL', '骨料销售模板', 'RETAIL', 1, 'PUBLISHED', '0', '商品销售场景骨料模板', 'admin', CURRENT_TIMESTAMP);

INSERT INTO template_field
(template_id, field_code, field_label, field_type, required_flag, default_value, options, validation_rule, indexed_flag, trace_flag, search_flag, export_flag, editable_flag, editable_event_types, sort_order, create_by, create_time)
VALUES
(1, 'aggregate_serial_no', '骨料串号', 'input', '1', NULL, '[]'::jsonb, '{"maxLength":64}'::jsonb, '1', '1', '1', '1', '0', '["BIND","CREATE"]'::jsonb, 1, 'admin', CURRENT_TIMESTAMP),
(1, 'product_model', '产品型号', 'input', '1', NULL, '[]'::jsonb, '{"maxLength":64}'::jsonb, '1', '1', '1', '1', '1', '["BIND","UPDATE"]'::jsonb, 2, 'admin', CURRENT_TIMESTAMP),
(1, 'manufacturer', '生产厂家', 'input', '1', NULL, '[]'::jsonb, '{"maxLength":128}'::jsonb, '0', '1', '1', '1', '1', '["BIND","UPDATE"]'::jsonb, 3, 'admin', CURRENT_TIMESTAMP),
(1, 'production_date', '生产日期', 'date', '1', NULL, '[]'::jsonb, '{"format":"date"}'::jsonb, '0', '1', '1', '1', '1', '["BIND","UPDATE"]'::jsonb, 4, 'admin', CURRENT_TIMESTAMP),
(1, 'project_name', '应用工程名称', 'input', '0', NULL, '[]'::jsonb, '{"maxLength":128}'::jsonb, '0', '1', '1', '1', '1', '["INBOUND","TRANSFER","INSTALL"]'::jsonb, 5, 'admin', CURRENT_TIMESTAMP),
(1, 'construction_time', '施工时间', 'date', '0', NULL, '[]'::jsonb, '{"format":"date"}'::jsonb, '0', '1', '0', '1', '1', '["INSTALL"]'::jsonb, 6, 'admin', CURRENT_TIMESTAMP),
(2, 'aggregate_serial_no', '骨料串号', 'input', '1', NULL, '[]'::jsonb, '{"maxLength":64}'::jsonb, '1', '1', '1', '1', '0', '["BIND","CREATE"]'::jsonb, 1, 'admin', CURRENT_TIMESTAMP),
(2, 'supplier_product_info', '供应商出库物品信息', 'textarea', '1', NULL, '[]'::jsonb, '{"maxLength":1000}'::jsonb, '0', '1', '1', '1', '1', '["OUTBOUND","TRANSFER"]'::jsonb, 2, 'admin', CURRENT_TIMESTAMP),
(2, 'supplier_out_time', '供应商出库时间', 'date', '0', NULL, '[]'::jsonb, '{"format":"date"}'::jsonb, '0', '1', '1', '1', '1', '["OUTBOUND"]'::jsonb, 3, 'admin', CURRENT_TIMESTAMP),
(2, 'receiver_name', '收货人', 'input', '0', NULL, '[]'::jsonb, '{"maxLength":64}'::jsonb, '0', '1', '1', '1', '1', '["INBOUND","SIGN"]'::jsonb, 4, 'admin', CURRENT_TIMESTAMP),
(3, 'digital_serial_no', '数字骨料串号', 'input', '1', NULL, '[]'::jsonb, '{"maxLength":64}'::jsonb, '1', '1', '1', '1', '0', '["BIND","CREATE"]'::jsonb, 1, 'admin', CURRENT_TIMESTAMP),
(3, 'product_model', '产品型号', 'input', '1', NULL, '[]'::jsonb, '{"maxLength":64}'::jsonb, '1', '1', '1', '1', '1', '["BIND","UPDATE"]'::jsonb, 2, 'admin', CURRENT_TIMESTAMP),
(3, 'quality_type', '品质类型', 'select', '0', NULL, '[{"label":"合格品","value":"合格品"},{"label":"样品","value":"样品"},{"label":"半成品","value":"半成品"}]'::jsonb, '{}'::jsonb, '0', '1', '1', '1', '1', '["BIND","QC"]'::jsonb, 3, 'admin', CURRENT_TIMESTAMP),
(3, 'dealer_payment_status', '经销商付款状态', 'select', '0', NULL, '[{"label":"已付款","value":"已付款"},{"label":"未付款","value":"未付款"},{"label":"部分付款","value":"部分付款"}]'::jsonb, '{}'::jsonb, '0', '1', '1', '1', '1', '["SALE","DELIVERY"]'::jsonb, 4, 'admin', CURRENT_TIMESTAMP),
(3, 'user_payment_status', '用户付款状态', 'select', '0', NULL, '[{"label":"已付款","value":"已付款"},{"label":"未付款","value":"未付款"},{"label":"部分付款","value":"部分付款"}]'::jsonb, '{}'::jsonb, '0', '1', '1', '1', '1', '["SALE","SIGN"]'::jsonb, 5, 'admin', CURRENT_TIMESTAMP);

-- Legacy aggregate_* compatibility schema used by the current backend

CREATE TABLE aggregate_material (
  material_id BIGSERIAL PRIMARY KEY,
  material_code VARCHAR(64) NOT NULL,
  material_name VARCHAR(128) NOT NULL,
  material_type VARCHAR(64),
  specification VARCHAR(128),
  origin_place VARCHAR(255),
  batch_no VARCHAR(64),
  state VARCHAR(2) DEFAULT '1',
  supplier_id BIGINT,
  supplier_name VARCHAR(128),
  unit VARCHAR(32) DEFAULT '吨',
  quality_grade VARCHAR(64),
  owner_user_id BIGINT,
  owner_user_name VARCHAR(64),
  rfid_count BIGINT DEFAULT 0,
  used_rfid_count BIGINT,
  unused_rfid_count BIGINT,
  del_flag CHAR(1) DEFAULT '0',
  create_by VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_by VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  remark VARCHAR(500)
);
CREATE INDEX idx_aggregate_material_code ON aggregate_material(material_code);
CREATE INDEX idx_aggregate_material_batch ON aggregate_material(batch_no);

CREATE TABLE aggregate_rfid_identity (
  identity_id BIGSERIAL PRIMARY KEY,
  rfid_code VARCHAR(128) NOT NULL UNIQUE,
  tid_code VARCHAR(128),
  material_id BIGINT NOT NULL,
  material_code VARCHAR(64),
  material_name VARCHAR(128),
  batch_no VARCHAR(64),
  identity_level VARCHAR(32) DEFAULT 'BATCH',
  current_state VARCHAR(32) DEFAULT 'CREATED',
  current_warehouse_id BIGINT,
  current_warehouse_name VARCHAR(128),
  owner_user_id BIGINT,
  owner_user_name VARCHAR(64),
  bind_goods_id BIGINT,
  bind_goods_code VARCHAR(64),
  bind_goods_name VARCHAR(128),
  bind_goods_time TIMESTAMP,
  bind_object_type VARCHAR(64),
  current_location VARCHAR(255),
  bind_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  last_event_time TIMESTAMP,
  del_flag CHAR(1) DEFAULT '0',
  create_by VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_by VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  remark VARCHAR(500)
);
CREATE INDEX idx_aggregate_rfid_material ON aggregate_rfid_identity(material_id);
CREATE INDEX idx_aggregate_rfid_state ON aggregate_rfid_identity(current_state);

CREATE TABLE aggregate_event (
  event_id BIGSERIAL PRIMARY KEY,
  identity_id BIGINT NOT NULL,
  rfid_code VARCHAR(128) NOT NULL,
  material_id BIGINT,
  object_id BIGINT,
  event_type VARCHAR(32) NOT NULL,
  event_name VARCHAR(64),
  event_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  location_name VARCHAR(255),
  source_module VARCHAR(64),
  longitude NUMERIC(12,8),
  latitude NUMERIC(12,8),
  operator_id BIGINT,
  operator_name VARCHAR(64),
  action_name VARCHAR(128),
  device_id BIGINT,
  device_code VARCHAR(64),
  device_type VARCHAR(32),
  warehouse_id BIGINT,
  warehouse_name VARCHAR(128),
  from_warehouse_id BIGINT,
  from_warehouse_name VARCHAR(128),
  to_warehouse_id BIGINT,
  to_warehouse_name VARCHAR(128),
  weight NUMERIC(12,3),
  vehicle_no VARCHAR(32),
  source_receipt_type VARCHAR(32),
  source_receipt_id BIGINT,
  source_receipt_no VARCHAR(64),
  raw_payload TEXT,
  snapshot_data TEXT,
  create_by VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  remark VARCHAR(500)
);
CREATE INDEX idx_aggregate_event_identity ON aggregate_event(identity_id);
CREATE INDEX idx_aggregate_event_rfid ON aggregate_event(rfid_code);
CREATE INDEX idx_aggregate_event_type_time ON aggregate_event(event_type, event_time);

CREATE TABLE aggregate_device (
  device_id BIGSERIAL PRIMARY KEY,
  device_code VARCHAR(64) NOT NULL UNIQUE,
  device_name VARCHAR(128) NOT NULL,
  device_type VARCHAR(32) NOT NULL,
  location_name VARCHAR(255),
  warehouse_id BIGINT,
  warehouse_name VARCHAR(128),
  status CHAR(1) DEFAULT '0',
  del_flag CHAR(1) DEFAULT '0',
  create_by VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_by VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  remark VARCHAR(500)
);

CREATE TABLE aggregate_subject_template (
  template_id BIGSERIAL PRIMARY KEY,
  subject_code VARCHAR(64) NOT NULL UNIQUE,
  subject_name VARCHAR(128) NOT NULL,
  module_name VARCHAR(128),
  status CHAR(1) DEFAULT '1',
  del_flag CHAR(1) DEFAULT '0',
  create_by VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_by VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  remark VARCHAR(500)
);

CREATE TABLE aggregate_subject_field (
  field_id BIGSERIAL PRIMARY KEY,
  template_id BIGINT NOT NULL,
  field_code VARCHAR(64) NOT NULL,
  field_label VARCHAR(128) NOT NULL,
  field_type VARCHAR(32) NOT NULL,
  placeholder VARCHAR(255),
  default_value VARCHAR(255),
  options_json TEXT,
  validation_rule VARCHAR(500),
  required_flag CHAR(1) DEFAULT '0',
  indexed_flag CHAR(1) DEFAULT '0',
  trace_flag CHAR(1) DEFAULT '0',
  search_flag CHAR(1) DEFAULT '0',
  export_flag CHAR(1) DEFAULT '0',
  write_scope VARCHAR(64) DEFAULT '1',
  editable_event_types VARCHAR(255),
  sort_order INT DEFAULT 1,
  del_flag CHAR(1) DEFAULT '0',
  create_by VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_by VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  remark VARCHAR(500)
);
CREATE INDEX idx_aggregate_subject_field_template ON aggregate_subject_field(template_id);

CREATE TABLE aggregate_subject_bind_record (
  record_id BIGSERIAL PRIMARY KEY,
  identity_id BIGINT NOT NULL,
  rfid_code VARCHAR(128) NOT NULL,
  bind_goods_id BIGINT,
  bind_goods_code VARCHAR(64),
  bind_goods_name VARCHAR(128),
  template_id BIGINT NOT NULL,
  subject_code VARCHAR(64),
  subject_name VARCHAR(128),
  module_name VARCHAR(128),
  write_no INT DEFAULT 1,
  form_data_json TEXT,
  field_snapshot_json TEXT,
  operator_id BIGINT,
  operator_name VARCHAR(64),
  write_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  object_id BIGINT,
  object_code VARCHAR(64),
  object_name VARCHAR(128),
  del_flag CHAR(1) DEFAULT '0',
  create_by VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_by VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  remark VARCHAR(500)
);
CREATE INDEX idx_aggregate_subject_bind_identity ON aggregate_subject_bind_record(identity_id);
CREATE INDEX idx_aggregate_subject_bind_template ON aggregate_subject_bind_record(template_id);

COMMENT ON TABLE aggregate_material IS '骨料基础档案';
COMMENT ON TABLE aggregate_rfid_identity IS '骨料RFID身份';
COMMENT ON TABLE aggregate_event IS '骨料RFID事件流水';
COMMENT ON TABLE aggregate_device IS '骨料采集设备';
COMMENT ON TABLE aggregate_subject_template IS '骨料模板';
COMMENT ON TABLE aggregate_subject_field IS '骨料模板字段';
COMMENT ON TABLE aggregate_subject_bind_record IS '骨料绑定记录';

INSERT INTO aggregate_subject_template
(template_id, subject_code, subject_name, module_name, status, create_by, create_time, remark)
VALUES
(1, 'PRECAST', '水泥预制品模块', '水泥预制品模块', '0', 'admin', CURRENT_TIMESTAMP, '按图片内置，可直接使用'),
(2, 'LOGISTICS', '物流模块', '物流模块', '1', 'admin', CURRENT_TIMESTAMP, '按图片内置，可直接使用'),
(3, 'RETAIL', '一般商品销售', '一般商品销售', '1', 'admin', CURRENT_TIMESTAMP, '按图片内置，可直接使用');

INSERT INTO aggregate_subject_field
(template_id, field_code, field_label, field_type, placeholder, required_flag, write_scope, sort_order, create_by, create_time)
VALUES
(1, 'aggregate_serial_no', '骨料串号', 'input', '请输入骨料串号', '1', '', 1, 'admin', CURRENT_TIMESTAMP),
(1, 'product_model', '产品型号', 'input', '请输入产品型号', '1', '', 2, 'admin', CURRENT_TIMESTAMP),
(1, 'manufacturer', '生产厂家', 'input', '请输入生产厂家', '1', '', 3, 'admin', CURRENT_TIMESTAMP),
(1, 'production_team', '生产班组', 'input', '请输入生产班组', '0', '', 4, 'admin', CURRENT_TIMESTAMP),
(1, 'production_date', '生产日期', 'date', '请选择生产日期', '1', '', 5, 'admin', CURRENT_TIMESTAMP),
(1, 'raw_material', '原材料', 'textarea', '请输入原材料说明', '0', '', 6, 'admin', CURRENT_TIMESTAMP),
(1, 'production_other_info', '生产其它信息', 'textarea', '请输入生产其它信息', '0', '', 7, 'admin', CURRENT_TIMESTAMP),
(1, 'producer_writer', '生产端写入人', 'input', '请输入生产端写入人', '0', '', 8, 'admin', CURRENT_TIMESTAMP),
(1, 'project_name', '应用工程名称', 'input', '请输入应用工程名称', '0', '', 9, 'admin', CURRENT_TIMESTAMP),
(1, 'project_owner', '工程甲方', 'input', '请输入工程甲方', '0', '', 10, 'admin', CURRENT_TIMESTAMP),
(1, 'construction_party', '施工方', 'input', '请输入施工方', '0', '', 11, 'admin', CURRENT_TIMESTAMP),
(1, 'construction_time', '施工时间', 'date', '请选择施工时间', '0', '', 12, 'admin', CURRENT_TIMESTAMP),
(1, 'geo_position', '地理坐标', 'input', '请输入地理坐标', '0', '', 13, 'admin', CURRENT_TIMESTAMP),
(1, 'buried_depth', '预埋深度', 'number', '请输入预埋深度', '0', '', 14, 'admin', CURRENT_TIMESTAMP),
(1, 'constructor_writer', '施工方写入人', 'input', '请输入施工方写入人', '0', '', 15, 'admin', CURRENT_TIMESTAMP),
(2, 'aggregate_serial_no', '骨料串号', 'input', '请输入骨料串号', '1', '', 1, 'admin', CURRENT_TIMESTAMP),
(2, 'supplier_product_info', '供应商出库物品名称、型号和数量、保质期', 'textarea', '请输入供应商出库物品信息', '1', '', 2, 'admin', CURRENT_TIMESTAMP),
(2, 'manufacturer_info', '生产厂家信息', 'textarea', '请输入生产厂家信息', '0', '', 3, 'admin', CURRENT_TIMESTAMP),
(2, 'supplier_info', '供应商信息', 'textarea', '请输入供应商信息', '0', '', 4, 'admin', CURRENT_TIMESTAMP),
(2, 'supplier_out_time', '供应商出库时间', 'date', '请选择供应商出库时间', '0', '', 5, 'admin', CURRENT_TIMESTAMP),
(2, 'supplier_out_user', '供应商出库人', 'input', '请输入供应商出库人', '0', '', 6, 'admin', CURRENT_TIMESTAMP),
(2, 'receiver_product_info', '收货方入库物品名称、型号、数量、保质期', 'textarea', '请输入收货方入库物品信息', '0', '', 7, 'admin', CURRENT_TIMESTAMP),
(2, 'receiver_name', '收货人', 'input', '请输入收货人', '0', '', 8, 'admin', CURRENT_TIMESTAMP),
(3, 'digital_serial_no', '数字骨料串号', 'input', '请输入数字骨料串号', '1', '', 1, 'admin', CURRENT_TIMESTAMP),
(3, 'manufacturer_info', '生产厂家信息', 'textarea', '请输入生产厂家信息', '1', '', 2, 'admin', CURRENT_TIMESTAMP),
(3, 'product_model', '产品型号', 'input', '请输入产品型号', '1', '', 3, 'admin', CURRENT_TIMESTAMP),
(3, 'production_date', '生产日期', 'date', '请选择生产日期', '1', '', 4, 'admin', CURRENT_TIMESTAMP),
(3, 'quality_type', '合格品/样品/半成品', 'select', '请选择品质类型', '0', '', 5, 'admin', CURRENT_TIMESTAMP),
(3, 'local_batch_count', '本批次数量', 'number', '请输入本批次数量', '0', '', 6, 'admin', CURRENT_TIMESTAMP),
(3, 'dealer_info', '经销商信息', 'textarea', '请输入经销商信息', '0', '', 7, 'admin', CURRENT_TIMESTAMP),
(3, 'dealer_buy_time', '经销商购货时间', 'date', '请选择经销商购货时间', '0', '', 8, 'admin', CURRENT_TIMESTAMP),
(3, 'dealer_payment_status', '经销商付款状态', 'select', '请选择经销商付款状态', '0', '', 9, 'admin', CURRENT_TIMESTAMP),
(3, 'dealer_sales_scope', '经销商销售范围', 'textarea', '请输入经销商销售范围', '0', '', 10, 'admin', CURRENT_TIMESTAMP),
(3, 'user_geo_info', '用户经纬度信息', 'input', '请输入用户经纬度信息', '0', '', 11, 'admin', CURRENT_TIMESTAMP),
(3, 'user_info', '用户信息', 'textarea', '请输入用户信息', '0', '', 12, 'admin', CURRENT_TIMESTAMP),
(3, 'user_payment_status', '用户付款状态', 'select', '请选择用户付款状态', '0', '', 13, 'admin', CURRENT_TIMESTAMP);

UPDATE aggregate_subject_field SET options_json='[{"label":"合格品","value":"合格品"},{"label":"样品","value":"样品"},{"label":"半成品","value":"半成品"}]' WHERE template_id=3 AND field_code='quality_type';
UPDATE aggregate_subject_field SET options_json='[{"label":"已付款","value":"已付款"},{"label":"未付款","value":"未付款"},{"label":"部分付款","value":"部分付款"}]' WHERE template_id=3 AND field_code IN ('dealer_payment_status', 'user_payment_status');

DELETE FROM sys_menu WHERE menu_id IN (2300,2301,2302,2303,2304,2305,2306,2311,2312,2313,2314,2315,2321,2322,2323,2324,2331,2341,2351,2352,2353,2354,2361,2362,2363);
INSERT INTO sys_menu VALUES (2300, '数字骨料平台', 0, 4, 'aggregate', NULL, '', 1, 0, 'M', '0', '0', '', 'tree', 'admin', CURRENT_TIMESTAMP, '', NULL, '基于模板、标签、事件、时间线的骨料可信管理平台');
INSERT INTO sys_menu VALUES (2301, '骨料档案', 2300, 1, 'material', 'warehouse/aggregate/material/index', '', 1, 0, 'C', '0', '0', 'warehouse:aggregate:material:list', 'list', 'admin', CURRENT_TIMESTAMP, '', NULL, '骨料档案管理');
INSERT INTO sys_menu VALUES (2302, '标签绑定', 2300, 2, 'rfid', 'warehouse/aggregate/rfid/index', '', 1, 0, 'C', '0', '0', 'warehouse:aggregate:rfid:list', 'component', 'admin', CURRENT_TIMESTAMP, '', NULL, '骨料标签绑定');
INSERT INTO sys_menu VALUES (2303, '事件中心', 2300, 3, 'event', 'warehouse/aggregate/event/index', '', 1, 0, 'C', '0', '0', 'warehouse:aggregate:event:list', 'form', 'admin', CURRENT_TIMESTAMP, '', NULL, '骨料事件中心');
INSERT INTO sys_menu VALUES (2304, '追踪时间线', 2300, 4, 'lifecycle', 'warehouse/aggregate/lifecycle/index', '', 1, 0, 'C', '0', '0', 'warehouse:aggregate:lifecycle:query', 'time-range', 'admin', CURRENT_TIMESTAMP, '', NULL, '骨料追踪时间线');
INSERT INTO sys_menu VALUES (2305, '模板管理', 2300, 5, 'subjectTemplate', 'warehouse/aggregate/subjectTemplate/index', '', 1, 0, 'C', '0', '0', 'warehouse:aggregate:subjectTemplate:list', 'build', 'admin', CURRENT_TIMESTAMP, '', NULL, '骨料模板管理');
INSERT INTO sys_menu VALUES (2306, '对象管理', 2300, 6, 'object', 'warehouse/aggregate/object/index', '', 1, 0, 'C', '0', '0', 'warehouse:aggregate:object:list', 'example', 'admin', CURRENT_TIMESTAMP, '', NULL, '可信对象管理');
INSERT INTO sys_menu VALUES (2311, '骨料查询', 2301, 1, '', '', '', 1, 0, 'F', '0', '0', 'warehouse:aggregate:material:query', '#', 'admin', CURRENT_TIMESTAMP, '', NULL, '');
INSERT INTO sys_menu VALUES (2312, '骨料新增', 2301, 2, '', '', '', 1, 0, 'F', '0', '0', 'warehouse:aggregate:material:add', '#', 'admin', CURRENT_TIMESTAMP, '', NULL, '');
INSERT INTO sys_menu VALUES (2313, '骨料修改', 2301, 3, '', '', '', 1, 0, 'F', '0', '0', 'warehouse:aggregate:material:edit', '#', 'admin', CURRENT_TIMESTAMP, '', NULL, '');
INSERT INTO sys_menu VALUES (2314, '骨料删除', 2301, 4, '', '', '', 1, 0, 'F', '0', '0', 'warehouse:aggregate:material:remove', '#', 'admin', CURRENT_TIMESTAMP, '', NULL, '');
INSERT INTO sys_menu VALUES (2315, '骨料批次创建', 2301, 5, '', '', '', 1, 0, 'F', '0', '0', 'warehouse:aggregate:material:import', '#', 'admin', CURRENT_TIMESTAMP, '', NULL, '');
INSERT INTO sys_menu VALUES (2321, '标签绑定查询', 2302, 1, '', '', '', 1, 0, 'F', '0', '0', 'warehouse:aggregate:rfid:query', '#', 'admin', CURRENT_TIMESTAMP, '', NULL, '');
INSERT INTO sys_menu VALUES (2322, '标签绑定新增', 2302, 2, '', '', '', 1, 0, 'F', '0', '0', 'warehouse:aggregate:rfid:add', '#', 'admin', CURRENT_TIMESTAMP, '', NULL, '');
INSERT INTO sys_menu VALUES (2323, '标签绑定修改', 2302, 3, '', '', '', 1, 0, 'F', '0', '0', 'warehouse:aggregate:rfid:edit', '#', 'admin', CURRENT_TIMESTAMP, '', NULL, '');
INSERT INTO sys_menu VALUES (2324, '标签绑定删除', 2302, 4, '', '', '', 1, 0, 'F', '0', '0', 'warehouse:aggregate:rfid:remove', '#', 'admin', CURRENT_TIMESTAMP, '', NULL, '');
INSERT INTO sys_menu VALUES (2331, '事件采集新增', 2303, 1, '', '', '', 1, 0, 'F', '0', '0', 'warehouse:aggregate:event:add', '#', 'admin', CURRENT_TIMESTAMP, '', NULL, '');
INSERT INTO sys_menu VALUES (2341, '追踪时间线查询', 2304, 1, '', '', '', 1, 0, 'F', '0', '0', 'warehouse:aggregate:lifecycle:query', '#', 'admin', CURRENT_TIMESTAMP, '', NULL, '');
INSERT INTO sys_menu VALUES (2351, '模板管理查询', 2305, 1, '', '', '', 1, 0, 'F', '0', '0', 'warehouse:aggregate:subjectTemplate:query', '#', 'admin', CURRENT_TIMESTAMP, '', NULL, '');
INSERT INTO sys_menu VALUES (2352, '模板管理新增', 2305, 2, '', '', '', 1, 0, 'F', '0', '0', 'warehouse:aggregate:subjectTemplate:add', '#', 'admin', CURRENT_TIMESTAMP, '', NULL, '');
INSERT INTO sys_menu VALUES (2353, '模板管理修改', 2305, 3, '', '', '', 1, 0, 'F', '0', '0', 'warehouse:aggregate:subjectTemplate:edit', '#', 'admin', CURRENT_TIMESTAMP, '', NULL, '');
INSERT INTO sys_menu VALUES (2354, '模板管理删除', 2305, 4, '', '', '', 1, 0, 'F', '0', '0', 'warehouse:aggregate:subjectTemplate:remove', '#', 'admin', CURRENT_TIMESTAMP, '', NULL, '');
INSERT INTO sys_menu VALUES (2361, '对象管理查询', 2306, 1, '', '', '', 1, 0, 'F', '0', '0', 'warehouse:aggregate:object:query', '#', 'admin', CURRENT_TIMESTAMP, '', NULL, '');
INSERT INTO sys_menu VALUES (2362, '对象管理新增', 2306, 2, '', '', '', 1, 0, 'F', '0', '0', 'warehouse:aggregate:object:add', '#', 'admin', CURRENT_TIMESTAMP, '', NULL, '');
INSERT INTO sys_menu VALUES (2363, '对象管理修改', 2306, 3, '', '', '', 1, 0, 'F', '0', '0', 'warehouse:aggregate:object:edit', '#', 'admin', CURRENT_TIMESTAMP, '', NULL, '');

CREATE OR REPLACE VIEW aggregate_object_timeline AS
SELECT
  ae.event_id,
  ae.identity_id,
  ae.rfid_code,
  ae.material_id AS object_id,
  ae.event_type,
  ae.event_name,
  ae.event_time,
  ae.operator_id,
  ae.operator_name,
  ae.location_name,
  ae.source_module,
  ae.snapshot_data,
  ae.raw_payload,
  ae.remark
FROM aggregate_event ae
ORDER BY ae.event_time DESC, ae.event_id DESC;
