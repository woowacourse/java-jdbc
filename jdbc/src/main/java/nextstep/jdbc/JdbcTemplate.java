package nextstep.jdbc;

import nextstep.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Map<String, Object>> queryForList(final String query) {
        return query(query, new ColumnMapRowMapper());
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        return query(sql, new RowMapperResultSetExtractor<>(rowMapper));
    }

    public <T> T query(final String sql, final ResultSetExtractor<T> rse) {
        return execute(new SimplePreparedStatement(sql), rse);
    }

    public void queryDML(final String query, @Nullable Object... args) {
        execute(new SimplePreparedStatement(query), new ArgumentPreparedStatementSetter(args));
    }

    private void execute(PreparedStatementCreator psc, @Nullable PreparedStatementSetter pss) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = dataSource.getConnection();
            pstmt = psc.createPreparedStatement(conn);
            pss.setValue(pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException ignored) {
            }

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }

    private <T> T execute(PreparedStatementCreator psc, final ResultSetExtractor<T> rse) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = dataSource.getConnection();
            pstmt = psc.createPreparedStatement(conn);
            final ResultSet resultSet = pstmt.executeQuery();
            return rse.extractData(resultSet);

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ignored) {
            }
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException ignored) {
            }

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }

    private static class SimplePreparedStatement implements PreparedStatementCreator {

        private final String sql;

        private SimplePreparedStatement(String sql) {
            Assert.notNull(sql, "SQL must not be null");
            this.sql = sql;
        }

        @Override
        public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
            return conn.prepareStatement(this.sql);
        }
    }
}
