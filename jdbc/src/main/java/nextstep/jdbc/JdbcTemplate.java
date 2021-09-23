package nextstep.jdbc;

import nextstep.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void queryDML(final String query, @Nullable Object... args) {
        execute(new SimplePreparedStatement(query), new ArgumentPreparedStatementSetter(args));
    }

    public void queryDML(final String query) {
        execute(new SimplePreparedStatement(query));
    }

    private void execute(PreparedStatementCreator psc, PreparedStatementSetter pss) {
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

    private void execute(PreparedStatementCreator stmt) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = dataSource.getConnection();
            pstmt = stmt.createPreparedStatement(conn);
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
