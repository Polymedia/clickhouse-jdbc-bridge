package com.clickhouse.jdbcbridge.impl;

import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.clickhouse.jdbcbridge.core.ColumnDefinition;
import com.clickhouse.jdbcbridge.core.DataType;
import com.clickhouse.jdbcbridge.core.DataTypeConverter;
import com.clickhouse.jdbcbridge.core.QueryParameters;
import com.clickhouse.jdbcbridge.core.TableDefinition;

/**
 * SQL Server specific helper to infer result set metadata without executing the query.
 *
 * @see https://learn.microsoft.com/en-us/sql/relational-databases/system-stored-procedures/sp-describe-first-result-set-transact-sql
 */
final class SqlServerMetadata {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SqlServerMetadata.class);

    private static final String COLUMN_PREFIX = "col_";

    private SqlServerMetadata() {
    }

    static boolean isSqlServer(Connection conn) {
        if (conn == null) {
            return false;
        }

        boolean isSqlServer = false;
        try {
            String product = conn.getMetaData().getDatabaseProductName();
            String url = conn.getMetaData().getURL();
            String driver = conn.getMetaData().getDriverName();

            String p = product == null ? null : product.toLowerCase();
            String u = url == null ? null : url.toLowerCase();
            String d = driver == null ? null : driver.toLowerCase();

            isSqlServer = (p != null && (p.contains("sql server")))
                    || (u != null && (u.startsWith("jdbc:sqlserver:")))
                    || (d != null && d.contains("sql server"));
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Failed to detect SQL Server database product", e);
            }
        }

        return isSqlServer;
    }

    private static String generateColumnName(int ordinal) {
        return new StringBuilder().append(COLUMN_PREFIX).append(ordinal).toString();
    }

    private static void setTimeout(PreparedStatement stmt, int expectedTimeout) {
        if (stmt == null || expectedTimeout < 0) {
            return;
        }

        int currentTimeout = 0;
        try {
            currentTimeout = stmt.getQueryTimeout();
        } catch (Exception e) {
        }

        if (currentTimeout != expectedTimeout) {
            try {
                stmt.setQueryTimeout(expectedTimeout);
            } catch (Exception e) {
                // ignore
            }
        }
    }

    private static String getSqlServerBaseType(String systemTypeName) {
        if (systemTypeName == null) {
            return null;
        }

        String s = systemTypeName.trim().toLowerCase();
        // examples: "nvarchar(100)", "decimal(18,2)", "datetime2(7)", "varchar(max)", "varchar(10) collate ..."
        int idx = s.indexOf('(');
        if (idx > 0) {
            s = s.substring(0, idx).trim();
        }
        idx = s.indexOf(' ');
        if (idx > 0) {
            s = s.substring(0, idx).trim();
        }

        return s;
    }

    private static JDBCType getSqlServerJdbcType(String baseType) {
        if (baseType == null || baseType.isEmpty()) {
            return JDBCType.OTHER;
        }

        switch (baseType) {
            case "bigint":
                return JDBCType.BIGINT;
            case "int":
                return JDBCType.INTEGER;
            case "smallint":
                return JDBCType.SMALLINT;
            case "tinyint":
                return JDBCType.TINYINT;
            case "bit":
                // map to BIT so converter returns Int8 when precision==1
                return JDBCType.BIT;
            case "decimal":
            case "numeric":
            case "money":
            case "smallmoney":
                return JDBCType.DECIMAL;
            case "real":
                return JDBCType.REAL;
            case "float":
                return JDBCType.DOUBLE;
            case "date":
                return JDBCType.DATE;
            case "time":
                return JDBCType.TIME;
            case "datetime":
            case "datetime2":
            case "smalldatetime":
                return JDBCType.TIMESTAMP;
            case "datetimeoffset":
                return JDBCType.TIMESTAMP_WITH_TIMEZONE;
            case "char":
            case "varchar":
            case "text":
            case "xml":
            case "uniqueidentifier":
                return JDBCType.VARCHAR;
            case "nchar":
            case "nvarchar":
            case "ntext":
                return JDBCType.NVARCHAR;
            case "binary":
            case "varbinary":
            case "image":
                return JDBCType.VARBINARY;
            default:
                return JDBCType.OTHER;
        }
    }

    static TableDefinition inferTypesByDescribe(Connection conn, String query, QueryParameters params,
            DataTypeConverter converter, int queryTimeout) throws SQLException {
        if (conn == null) {
            throw new SQLException("Connection is null");
        }
        if (query == null || query.isEmpty()) {
            throw new SQLException("Query is empty");
        }
        if (converter == null) {
            throw new SQLException("DataTypeConverter is null");
        }

        try (PreparedStatement ps = conn.prepareStatement("EXEC sp_describe_first_result_set @tsql = ?")) {
            setTimeout(ps, queryTimeout);
            ps.setString(1, query);

            try (ResultSet rs = ps.executeQuery()) {
                java.util.ArrayList<ColumnDefinition> cols = new java.util.ArrayList<>();

                while (rs.next()) {
                    boolean hidden = false;
                    try {
                        hidden = rs.getBoolean("is_hidden");
                    } catch (SQLException e) {
                        // ignore
                    }
                    if (hidden) {
                        continue;
                    }

                    int ordinal = 0;
                    try {
                        ordinal = rs.getInt("column_ordinal");
                    } catch (SQLException e) {
                        // ignore
                    }
                    if (ordinal <= 0) {
                        continue;
                    }

                    String name = null;
                    try {
                        name = rs.getString("name");
                    } catch (SQLException e) {
                        // ignore
                    }
                    if (name == null || name.isEmpty()) {
                        name = generateColumnName(ordinal);
                    }

                    String systemTypeName = null;
                    try {
                        systemTypeName = rs.getString("system_type_name");
                    } catch (SQLException e) {
                        // ignore
                    }

                    String baseType = getSqlServerBaseType(systemTypeName);
                    JDBCType jdbcType = getSqlServerJdbcType(baseType);

                    boolean nullable = false;
                    int length = 0;
                    int precision = 0;
                    int scale = 0;
                    boolean signed = true;

                    try {
                        nullable = rs.getBoolean("is_nullable");
                    } catch (SQLException e) {
                        // ignore
                    }
                    try {
                        length = rs.getInt("max_length");
                    } catch (SQLException e) {
                        // ignore
                    }
                    try {
                        precision = rs.getInt("precision");
                    } catch (SQLException e) {
                        // ignore
                    }
                    try {
                        scale = rs.getInt("scale");
                    } catch (SQLException e) {
                        // ignore
                    }
                    try {
                        signed = rs.getBoolean("is_signed");
                    } catch (SQLException e) {
                        // ignore
                    }

                    // nvarchar/nchar max_length is in bytes
                    if (length > 0
                            && ("nvarchar".equals(baseType) || "nchar".equals(baseType) || "ntext".equals(baseType))) {
                        length = length / 2;
                    }
                    if (length < 0) {
                        length = 0;
                    }

                    // SQL Server tinyint is unsigned (0..255)
                    if ("tinyint".equals(baseType)) {
                        signed = false;
                    }
                    // BIT should behave like boolean(0/1) - map precision to 1 to get Int8
                    if ("bit".equals(baseType) && precision <= 0) {
                        precision = 1;
                    }

                    DataType type = converter.from(jdbcType, baseType == null ? systemTypeName : baseType, precision,
                            scale, signed);

                    while (cols.size() < ordinal) {
                        cols.add(null);
                    }
                    cols.set(ordinal - 1, new ColumnDefinition(name, type, nullable, length, precision, scale));
                }

                java.util.ArrayList<ColumnDefinition> out = new java.util.ArrayList<>(cols.size());
                for (ColumnDefinition c : cols) {
                    if (c != null) {
                        out.add(c);
                    }
                }

                return new TableDefinition(out.toArray(new ColumnDefinition[out.size()]));
            }
        }
    }
}
