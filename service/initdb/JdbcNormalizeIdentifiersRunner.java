
import java.sql.*;
import java.util.*;

public class JdbcNormalizeIdentifiersRunner {
    public static void main(String[] args) throws Exception {
        if (args.length < 3) throw new IllegalArgumentException("args: pgUrl pgUser pgPass");
        Class.forName("org.postgresql.Driver");
        try (Connection conn = DriverManager.getConnection(args[0], args[1], args[2])) {
            conn.setAutoCommit(false);
            normalizeColumns(conn);
            conn.commit();
        }
        System.out.println("normalized");
    }

    private static void normalizeColumns(Connection conn) throws Exception {
        List<String[]> renames = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "select table_name, column_name from information_schema.columns where table_schema='public' order by table_name, ordinal_position");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String table = rs.getString(1);
                String column = rs.getString(2);
                String lower = column.toLowerCase(Locale.ROOT);
                if (!column.equals(lower)) {
                    renames.add(new String[]{table, column, lower});
                }
            }
        }
        try (Statement st = conn.createStatement()) {
            for (String[] item : renames) {
                String table = item[0];
                String oldCol = item[1];
                String newCol = item[2];
                try {
                    st.execute("ALTER TABLE \"" + table + "\" RENAME COLUMN \"" + oldCol + "\" TO \"" + newCol + "\"");
                } catch (Exception ignored) {
                }
            }
        }
    }
}
