package nextstep.jdbc;

import nextstep.exception.JdbcInternalException;
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

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Map<String, Object>> queryForList(final String sql) {
        return query(sql, new ColumnMapRowMapper());
    }

    public List<Map<String, Object>> queryForList(final String sql, @Nullable Object... args) {
        return query(sql, args, new ColumnMapRowMapper());
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        return query(sql, new RowMapperResultSetExtractor<>(rowMapper));
    }

    public <T> List<T> query(String sql, @Nullable Object[] args, RowMapper<T> rowMapper) {
        return query(sql, args, new RowMapperResultSetExtractor<>(rowMapper));
    }

    public <T> T query(final String sql, final ResultSetExtractor<T> rse) {
        return execute(new SimplePreparedStatement(sql), rse);
    }

    public <T> T query(final String sql, @Nullable Object[] args, final ResultSetExtractor<T> rse) {
        return execute(new SimplePreparedStatement(sql), new ArgumentPreparedStatementSetter(args), rse);
    }

    public void query(final String sql, @Nullable Object... args) {
        execute(new SimplePreparedStatement(sql), new ArgumentPreparedStatementSetter(args));
    }

    private void execute(PreparedStatementCreator psc, @Nullable PreparedStatementSetter pss) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = psc.createPreparedStatement(conn)
        ) {
            pss.setValue(pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.info("JdbcInternalException: {} {}", e.getMessage(), e);
            throw new JdbcInternalException("JdbcInternalException: " + e.getMessage(), e.getCause());
        }
    }

    private <T> T execute(PreparedStatementCreator psc, final ResultSetExtractor<T> rse) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = psc.createPreparedStatement(conn);
             ResultSet resultSet = pstmt.executeQuery()
        ) {
            return rse.extractData(resultSet);
        } catch (SQLException e) {
            log.info("JdbcInternalException: {} {}", e.getMessage(), e);
            throw new JdbcInternalException("JdbcInternalException: " + e.getMessage(), e.getCause());
        }
    }

    private <T> T execute(PreparedStatementCreator psc, PreparedStatementSetter pss, ResultSetExtractor<T> rse) {
        ResultSet rs = null;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = psc.createPreparedStatement(conn);
        ) {
            pss.setValue(pstmt);
            rs = pstmt.executeQuery();
            return rse.extractData(rs);
        } catch (SQLException e) {
            log.info("JdbcInternalException: {} {}", e.getMessage(), e);
            throw new JdbcInternalException("JdbcInternalException: " + e.getMessage(), e.getCause());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
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
