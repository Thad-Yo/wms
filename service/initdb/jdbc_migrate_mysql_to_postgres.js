const fs = require('fs');
const path = require('path');
const { spawnSync } = require('child_process');

const baseDir = '/home/jiaxiaofei/code/wms/service';
const mysqlJar = path.join(process.env.HOME, '.m2/repository/mysql/mysql-connector-java/8.0.29/mysql-connector-java-8.0.29.jar');
const pgJar = path.join(process.env.HOME, '.m2/repository/org/postgresql/postgresql/42.2.25/postgresql-42.2.25.jar');
const runnerJava = path.join(baseDir, 'initdb', 'JdbcFullMigrationRunner.java');
const pgInit = path.join(baseDir, 'initdb', 'aggregate_postgresql_init.sql');

const sourceUrl = 'jdbc:mysql://47.105.126.117:3306/ware_house?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=Asia/Shanghai';
const sourceUser = 'root';
const sourcePass = 'lilishop';
const targetUrl = 'jdbc:postgresql://47.105.126.117:5433/rfid_db';
const targetUser = 'rfid_admin';
const targetPass = 'rfid_pass123';

const javaSource = `
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

public class JdbcFullMigrationRunner {
    public static void main(String[] args) throws Exception {
        if (args.length < 7) {
            throw new IllegalArgumentException("args: mysqlUrl mysqlUser mysqlPass pgUrl pgUser pgPass initSqlPath");
        }
        Class.forName("com.mysql.cj.jdbc.Driver");
        Class.forName("org.postgresql.Driver");
        String mysqlUrl = args[0];
        String mysqlUser = args[1];
        String mysqlPass = args[2];
        String pgUrl = args[3];
        String pgUser = args[4];
        String pgPass = args[5];
        String initSqlPath = args[6];

        try (Connection mysql = DriverManager.getConnection(mysqlUrl, mysqlUser, mysqlPass);
             Connection pg = DriverManager.getConnection(pgUrl, pgUser, pgPass)) {
            pg.setAutoCommit(true);
            dropAllTables(pg);
            createCompatibilityObjects(pg);
            migrateSchema(mysql, pg);
            migrateData(mysql, pg);
            executeSqlFile(pg, initSqlPath);
        }
        System.out.println("migration finished");
    }

    private static void dropAllTables(Connection pg) throws Exception {
        try (Statement st = pg.createStatement();
             ResultSet rs = st.executeQuery("select tablename from pg_tables where schemaname='public'")) {
            List<String> tables = new ArrayList<>();
            while (rs.next()) tables.add(rs.getString(1));
            try (Statement drop = pg.createStatement()) {
                for (String table : tables) {
                    drop.execute("DROP TABLE IF EXISTS \\"" + table + "\\" CASCADE");
                }
            }
        }
    }

    private static void createCompatibilityObjects(Connection pg) throws Exception {
        String sql =
            "CREATE OR REPLACE FUNCTION sysdate() RETURNS TIMESTAMP AS $$ BEGIN RETURN CURRENT_TIMESTAMP; END; $$ LANGUAGE plpgsql;" +
            "CREATE OR REPLACE FUNCTION ifnull(TEXT, TEXT) RETURNS TEXT AS $$ BEGIN RETURN COALESCE($1,$2); END; $$ LANGUAGE plpgsql IMMUTABLE;" +
            "CREATE OR REPLACE FUNCTION ifnull(VARCHAR, VARCHAR) RETURNS VARCHAR AS $$ BEGIN RETURN COALESCE($1,$2); END; $$ LANGUAGE plpgsql IMMUTABLE;" +
            "CREATE OR REPLACE FUNCTION find_in_set(needle TEXT, haystack TEXT) RETURNS INTEGER AS $$ DECLARE items TEXT[]; pos INTEGER; BEGIN IF needle IS NULL OR haystack IS NULL OR haystack='' THEN RETURN 0; END IF; items:=string_to_array(haystack, ','); pos:=array_position(items, needle); RETURN COALESCE(pos,0); END; $$ LANGUAGE plpgsql IMMUTABLE;" +
            "CREATE OR REPLACE FUNCTION date_format(ts TIMESTAMP, fmt TEXT) RETURNS TEXT AS $$ BEGIN IF ts IS NULL THEN RETURN NULL; END IF; IF fmt='%y%m%d' THEN RETURN to_char(ts, 'YYMMDD'); ELSIF fmt='%Y-%m-%d' THEN RETURN to_char(ts,'YYYY-MM-DD'); ELSIF fmt='%Y-%m-%d %H:%i:%s' THEN RETURN to_char(ts,'YYYY-MM-DD HH24:MI:SS'); END IF; RETURN to_char(ts,'YYYY-MM-DD HH24:MI:SS'); END; $$ LANGUAGE plpgsql IMMUTABLE;" +
            "CREATE OR REPLACE FUNCTION timestampdiff(unit_name TEXT, start_time TIMESTAMP, end_time TIMESTAMP) RETURNS BIGINT AS $$ DECLARE diff_seconds NUMERIC; BEGIN IF start_time IS NULL OR end_time IS NULL THEN RETURN NULL; END IF; diff_seconds:=EXTRACT(EPOCH FROM (end_time-start_time)); CASE UPPER(unit_name) WHEN 'SECOND' THEN RETURN diff_seconds::BIGINT; WHEN 'MINUTE' THEN RETURN FLOOR(diff_seconds/60)::BIGINT; WHEN 'HOUR' THEN RETURN FLOOR(diff_seconds/3600)::BIGINT; WHEN 'DAY' THEN RETURN FLOOR(diff_seconds/86400)::BIGINT; ELSE RETURN diff_seconds::BIGINT; END CASE; END; $$ LANGUAGE plpgsql IMMUTABLE;" +
            "DROP VIEW IF EXISTS dual; CREATE VIEW dual AS SELECT 1 AS dummy;";
        for (String s : sql.split(";")) {
            if (s.trim().isEmpty()) continue;
            try (Statement st = pg.createStatement()) {
                st.execute(s);
            } catch (Exception ignored) {
            }
        }
    }

    private static void migrateSchema(Connection mysql, Connection pg) throws Exception {
        DatabaseMetaData meta = mysql.getMetaData();
        try (ResultSet tables = meta.getTables(mysql.getCatalog(), null, "%", new String[]{"TABLE"})) {
            while (tables.next()) {
                String table = tables.getString("TABLE_NAME");
                if (table == null) continue;
                System.out.println("create table " + table);
                createTable(meta, pg, mysql.getCatalog(), table);
            }
        }
    }

    private static void createTable(DatabaseMetaData meta, Connection pg, String catalog, String table) throws Exception {
        List<String> defs = new ArrayList<>();
        List<String> pkCols = new ArrayList<>();
        try (ResultSet pk = meta.getPrimaryKeys(catalog, null, table)) {
            while (pk.next()) pkCols.add(pk.getString("COLUMN_NAME"));
        }
        Map<String, Integer> autoCols = new HashMap<>();
        try (Statement st = meta.getConnection().createStatement();
             ResultSet rs = st.executeQuery("SHOW COLUMNS FROM " + table)) {
            while (rs.next()) {
                String field = rs.getString("Field");
                String extra = rs.getString("Extra");
                if (extra != null && extra.toLowerCase().contains("auto_increment")) {
                    autoCols.put(field, 1);
                }
            }
        } catch (Exception ignored) {}

        try (ResultSet cols = meta.getColumns(catalog, null, table, "%")) {
            while (cols.next()) {
                String col = cols.getString("COLUMN_NAME");
                int type = cols.getInt("DATA_TYPE");
                String typeName = mapType(type, cols.getInt("COLUMN_SIZE"), cols.getInt("DECIMAL_DIGITS"), autoCols.containsKey(col));
                boolean nullable = cols.getInt("NULLABLE") == DatabaseMetaData.columnNullable;
                String def = "\\"" + col + "\\" " + typeName + (nullable ? "" : " NOT NULL");
                String defVal = cols.getString("COLUMN_DEF");
                if (defVal != null && !typeName.contains("SERIAL")) {
                    def += " DEFAULT " + normalizeDefault(defVal);
                }
                defs.add(def);
            }
        }
        if (!pkCols.isEmpty()) {
            List<String> quoted = new ArrayList<>();
            for (String c : pkCols) quoted.add("\\"" + c + "\\"");
            defs.add("PRIMARY KEY (" + String.join(",", quoted) + ")");
        }
        String sql = "CREATE TABLE \\"" + table + "\\" (\\n  " + String.join(",\\n  ", defs) + "\\n)";
        try (Statement st = pg.createStatement()) {
            st.execute(sql);
        }
    }

    private static String mapType(int jdbcType, int size, int scale, boolean auto) {
        switch (jdbcType) {
            case Types.BIGINT: return auto ? "BIGSERIAL" : "BIGINT";
            case Types.INTEGER: return auto ? "SERIAL" : "INTEGER";
            case Types.TINYINT:
            case Types.SMALLINT: return "SMALLINT";
            case Types.FLOAT:
            case Types.REAL:
            case Types.DOUBLE: return "DOUBLE PRECISION";
            case Types.DECIMAL:
            case Types.NUMERIC: return scale > 0 ? "NUMERIC(" + size + "," + scale + ")" : "NUMERIC(" + Math.max(size, 18) + ",0)";
            case Types.TIMESTAMP: return "TIMESTAMP";
            case Types.DATE: return "DATE";
            case Types.TIME: return "TIME";
            case Types.BLOB:
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY: return "BYTEA";
            case Types.CLOB:
            case Types.LONGVARCHAR: return "TEXT";
            case Types.CHAR: return "CHAR(" + Math.max(size, 1) + ")";
            case Types.VARCHAR: return size > 0 && size < 10000 ? "VARCHAR(" + size + ")" : "TEXT";
            case Types.BIT:
            case Types.BOOLEAN: return "SMALLINT";
            default: return "TEXT";
        }
    }

    private static String normalizeDefault(String defVal) {
        String v = defVal.trim();
        if ("CURRENT_TIMESTAMP".equalsIgnoreCase(v) || v.startsWith("CURRENT_TIMESTAMP")) return "CURRENT_TIMESTAMP";
        if (v.startsWith("'") && v.endsWith("'")) return v;
        if (v.matches("-?\\\\d+(\\\\.\\\\d+)?")) return v;
        return "'" + v.replace("'", "''") + "'";
    }

    private static void migrateData(Connection mysql, Connection pg) throws Exception {
        DatabaseMetaData meta = mysql.getMetaData();
        try (ResultSet tables = meta.getTables(mysql.getCatalog(), null, "%", new String[]{"TABLE"})) {
            while (tables.next()) {
                String table = tables.getString("TABLE_NAME");
                if (table == null) continue;
                System.out.println("migrating data " + table);
                copyTable(mysql, pg, table);
            }
        }
    }

    private static void copyTable(Connection mysql, Connection pg, String table) throws Exception {
        List<String> columns = new ArrayList<>();
        try (Statement st = mysql.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM " + table + " LIMIT 1")) {
            ResultSetMetaData md = rs.getMetaData();
            for (int i = 1; i <= md.getColumnCount(); i++) columns.add(md.getColumnName(i));
        }
        String insertSql = "INSERT INTO \\"" + table + "\\" (" + quoteJoin(columns) + ") VALUES (" + String.join(",", Collections.nCopies(columns.size(), "?")) + ")";
        try (Statement read = mysql.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            read.setFetchSize(Integer.MIN_VALUE);
            try (ResultSet rs = read.executeQuery("SELECT * FROM " + table);
                 PreparedStatement ps = pg.prepareStatement(insertSql)) {
                int batch = 0;
                while (rs.next()) {
                    for (int i = 0; i < columns.size(); i++) {
                        Object val = rs.getObject(i + 1);
                        if (val instanceof byte[]) ps.setBytes(i + 1, (byte[]) val);
                        else ps.setObject(i + 1, val);
                    }
                    ps.addBatch();
                    batch++;
                    if (batch % 300 == 0) ps.executeBatch();
                }
                ps.executeBatch();
            }
        } catch (Exception e) {
            System.err.println("skip data for table " + table + ": " + e.getMessage());
        }
    }

    private static String quoteJoin(List<String> cols) {
        List<String> out = new ArrayList<>();
        for (String c : cols) out.add("\\"" + c + "\\"");
        return String.join(",", out);
    }

    private static void executeSqlFile(Connection conn, String filePath) throws Exception {
        String sql = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
        List<String> statements = splitStatements(sql);
        for (String s : statements) {
            String stmt = s.trim();
            if (stmt.isEmpty() || stmt.startsWith("--")) continue;
            try (Statement st = conn.createStatement()) {
                st.execute(stmt);
            }
        }
    }

    private static List<String> splitStatements(String sql) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inDollar = false;
        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);
            if (i + 1 < sql.length() && sql.charAt(i) == '$' && sql.charAt(i + 1) == '$') {
                inDollar = !inDollar;
                cur.append("$$");
                i++;
                continue;
            }
            if (c == ';' && !inDollar) {
                out.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        if (cur.length() > 0) out.add(cur.toString());
        return out;
    }
}
`;

fs.writeFileSync(runnerJava, javaSource, 'utf8');

const compile = spawnSync('javac', ['-cp', `${mysqlJar}:${pgJar}`, runnerJava], { stdio: 'inherit' });
if (compile.status !== 0) process.exit(compile.status || 1);

const run = spawnSync('java', ['-cp', `${baseDir}/initdb:${mysqlJar}:${pgJar}`, 'JdbcFullMigrationRunner', sourceUrl, sourceUser, sourcePass, targetUrl, targetUser, targetPass, pgInit], { stdio: 'inherit' });
process.exit(run.status || 0);
