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

    public List<Map<String, Object>> queryForList(final String sql, @Nullable Object... args) {
        return query(sql, new ColumnMapRowMapper(), args);
    }

    public <T> T queryForObject(final String sql, RowMapper<T> rowMapper,  @Nullable Object... args) {
        final List<T> query = query(sql, new RowMapperResultSetExtractor<>(rowMapper), args);
        return query.iterator().next();
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, @Nullable Object[] args) {
        return query(sql, new RowMapperResultSetExtractor<>(rowMapper), args);
    }

    public <T> T query(final String sql, final ResultSetExtractor<T> rse, @Nullable Object[] args) {
        return query(new SimplePreparedStatement(sql), new ArgumentPreparedStatementSetter(args), rse);
    }

    public <T> T query(PreparedStatementCreator psc, @Nullable final PreparedStatementSetter pss, final ResultSetExtractor<T> rss) {
        return execute(psc, pstmt -> {
            ResultSet rs = null;
            try {
                if (pss != null) {
                    pss.setValue(pstmt);
                }
                rs = pstmt.executeQuery();
                return rss.extractData(rs);
            } finally {
                try {
                    if (rs != null) {
                        rs.close();
                    }
                } catch (SQLException ignored) {
                }
            }
        });
    }

    public int update(final String sql, @Nullable Object... args) {
       return update(new SimplePreparedStatement(sql), new ArgumentPreparedStatementSetter(args));
    }

    public int update(final PreparedStatementCreator psc, @Nullable PreparedStatementSetter pss) {
        return execute(psc, pstmt -> {
            if (pss != null) {
                pss.setValue(pstmt);
            }
            return pstmt.executeUpdate();
        });
    }

    private <T> T execute(PreparedStatementCreator psc, PreparedStatementCallback<T> action) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = psc.createPreparedStatement(conn)
        ) {
            return action.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            log.info("JdbcInternalException: {} {}", e.getMessage(), e);
            throw new JdbcInternalException("JdbcInternalException: " + e.getMessage(), e.getCause());
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
