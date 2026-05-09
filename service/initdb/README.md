# PostgreSQL InitDB

本目录用于维护当前 WMS 项目的 PostgreSQL 初始化脚本，并初始化“基于模板、标签、事件、时间线的骨料追踪平台”模块。

## 文件

- `aggregate_postgresql_init.sql`
  - 包含 PostgreSQL 单库初始化所需的骨料追踪平台表
  - 包含新版核心表：`bone_rfid`、`object`、`template`、`template_field`、`object_event`
  - 包含当前后端兼容表：`aggregate_*`
  - 包含默认模板、菜单权限、时间线视图、事件哈希链触发器
- `mysql_to_postgres_dump.js`
  - 早期迁移阶段用于把 MySQL 导出内容转为 PostgreSQL 可导入格式

## 已覆盖内容

- 新版核心模型
  - `bone_rfid`
  - `"object"`
  - `template`
  - `template_field`
  - `object_event`
- 当前兼容模型
  - `aggregate_material`
  - `aggregate_rfid_identity`
  - `aggregate_event`
  - `aggregate_device`
  - `aggregate_subject_template`
  - `aggregate_subject_field`
  - `aggregate_subject_bind_record`
- 辅助对象
  - `bone_object_timeline`
  - `aggregate_object_timeline`
  - `sys_menu` 中骨料平台菜单

## 导入方式

```bash
psql -h <host> -U <user> -d <database> -f aggregate_postgresql_init.sql
```

## 当前定位

当前项目已经以 PostgreSQL 作为唯一数据库维护目标。

- 日常初始化：直接执行 `aggregate_postgresql_init.sql`
- 当前后端：优先运行兼容表 `aggregate_*`
- 后续演进：逐步切换到 `bone_rfid / object / template / template_field / object_event`

## 当前后端改造状态

- 已切换 `warehouse-admin` JDBC 驱动为 PostgreSQL
- 已切换 `application-dev.yml` / `application-test.yml` / `application-prod.yml` 为 PostgreSQL 配置
- 已切换 MyBatis Plus 分页方言为 PostgreSQL
- 已清理一轮 PostgreSQL 不兼容的 Mapper XML 语法与字符型比较问题
- 已在 PostgreSQL init 脚本中补充兼容函数：
  - `sysdate()`
  - `ifnull()`
  - `find_in_set()`
  - `date_format()`
  - `timestampdiff()`
  - `dual` 兼容视图
- 当前后端已经完成 PostgreSQL 方向编译验证

## 注意事项

1. 该脚本默认 PostgreSQL 数据库中已经存在 RuoYi/WMS 主库基础表，尤其是 `sys_menu`。
2. 当前脚本同时维护“新版核心模型”和“现有兼容模型”，是为了保证你当前系统可以继续平滑运行。
3. 兼容层里仍保留了 `aggregate_*` 与部分 `bind_goods_*` 命名，后续会继续逐步切到新版模型。
4. `object_event` 已支持哈希链字段和自动生成触发器，可作为后续可信溯源底座。
