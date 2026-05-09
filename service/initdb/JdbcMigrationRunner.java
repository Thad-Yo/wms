
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

public class JdbcMigrationRunner {
    private static final Set<String> SKIP_TABLES = new HashSet<>(Arrays.asList(
        "qrtz_blob_triggers","qrtz_calendars","qrtz_cron_triggers","qrtz_fired_triggers","qrtz_job_details",
        "qrtz_locks","qrtz_paused_trigger_grps","qrtz_scheduler_state","qrtz_simple_triggers","qrtz_simprop_triggers","qrtz_triggers"
    ));

    public static void main(String[] args) throws Exception {
        if (args.length < 7) {
            throw new IllegalArgumentException("args: mysqlUrl mysqlUser mysqlPass pgUrl pgUser pgPass initSqlPath");
        }
        String mysqlUrl = args[0];
        String mysqlUser = args[1];
        String mysqlPass = args[2];
        String pgUrl = args[3];
        String pgUser = args[4];
        String pgPass = args[5];
        String initSqlPath = args[6];

        Class.forName("com.mysql.cj.jdbc.Driver");
        Class.forName("org.postgresql.Driver");

        try (Connection mysql = DriverManager.getConnection(mysqlUrl, mysqlUser, mysqlPass);
             Connection pg = DriverManager.getConnection(pgUrl, pgUser, pgPass)) {
            pg.setAutoCommit(false);
            DatabaseMetaData meta = mysql.getMetaData();
            try (ResultSet rs = meta.getTables(mysql.getCatalog(), null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    String table = rs.getString("TABLE_NAME");
                    if (table == null || table.startsWith("aggregate_") || SKIP_TABLES.contains(table.toLowerCase())) {
                        continue;
                    }
                    migrateTable(mysql, pg, table);
                }
            }
            pg.commit();
        }

        try (Connection pg = DriverManager.getConnection(pgUrl, pgUser, pgPass)) {
            pg.setAutoCommit(true);
            executeSqlFile(pg, initSqlPath);
        }
        System.out.println("migration finished");
    }

    private static void executeSqlFile(Connection conn, String filePath) throws Exception {
        String sql = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        boolean inDollar = false;
        for (String line : sql.split("\n")) {
            String trim = line.trim();
            if (trim.startsWith("--")) {
                continue;
            }
            if (trim.contains("$$")) {
                inDollar = !inDollar;
            }
            sb.append(line).append('\n');
        }
        List<String> statements = splitStatements(sb.toString());
        for (String statement : statements) {
            String s = statement.trim();
            if (s.isEmpty()) continue;
            try (Statement st = conn.createStatement()) {
                st.execute(s);
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

    private static void migrateTable(Connection mysql, Connection pg, String table) throws Exception {
        System.out.println("migrating " + table);
        List<String> columns = new ArrayList<>();
        List<Integer> types = new ArrayList<>();
        try (Statement st = mysql.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM " + table + " LIMIT 1")) {
            ResultSetMetaData md = rs.getMetaData();
            for (int i = 1; i <= md.getColumnCount(); i++) {
                columns.add(md.getColumnName(i));
                types.add(md.getColumnType(i));
            }
        }
        String quotedCols = quoteJoin(columns);
        String placeholders = String.join(",", Collections.nCopies(columns.size(), "?"));
        String insertSql = "INSERT INTO \"" + table + "\" (" + quotedCols + ") VALUES (" + placeholders + ")";
        try (Statement read = mysql.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            read.setFetchSize(Integer.MIN_VALUE);
            try (ResultSet rs = read.executeQuery("SELECT * FROM " + table);
                 PreparedStatement ps = pg.prepareStatement(insertSql)) {
                int batch = 0;
                while (rs.next()) {
                    for (int i = 0; i < columns.size(); i++) {
                        Object val = rs.getObject(i + 1);
                        bind(ps, i + 1, val, types.get(i));
                    }
                    ps.addBatch();
                    batch++;
                    if (batch % 500 == 0) {
                        ps.executeBatch();
                    }
                }
                ps.executeBatch();
            }
        } catch (Exception e) {
            System.err.println("skip table " + table + " because: " + e.getMessage());
            pg.rollback();
            pg.setAutoCommit(false);
        }
    }

    private static String quoteJoin(List<String> cols) {
        List<String> out = new ArrayList<>();
        for (String c : cols) out.add("\"" + c + "\"");
        return String.join(",", out);
    }

    private static void bind(PreparedStatement ps, int idx, Object val, int jdbcType) throws Exception {
        if (val == null) {
            ps.setObject(idx, null);
            return;
        }
        if (val instanceof byte[]) {
            ps.setBytes(idx, (byte[]) val);
            return;
        }
        if (val instanceof java.sql.Timestamp) {
            ps.setTimestamp(idx, (Timestamp) val);
            return;
        }
        if (val instanceof java.sql.Date) {
            ps.setDate(idx, (java.sql.Date) val);
            return;
        }
        if (val instanceof java.sql.Time) {
            ps.setTime(idx, (java.sql.Time) val);
            return;
        }
        if (val instanceof BigDecimal) {
            ps.setBigDecimal(idx, (BigDecimal) val);
            return;
        }
        ps.setObject(idx, val);
    }
}
