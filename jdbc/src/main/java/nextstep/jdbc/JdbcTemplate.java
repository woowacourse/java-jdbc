package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.core.PreparedStatementCallback;
import nextstep.jdbc.core.RowMapper;
import nextstep.jdbc.core.RowMapperResultSetExtractor;
import nextstep.jdbc.support.DataAccessUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, final Object... args) {
        execute(sql, pstmt -> {
            setArguments(pstmt, args);
            return pstmt.executeUpdate();
        });
    }

    public void update(Connection connection, String sql, final Object... args) {
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            setArguments(pstmt, args);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.info("jdbcTemplate query:{}", sql);
            throw new DataAccessException(e);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        return DataAccessUtils.uniqueResult(query(sql, new RowMapperResultSetExtractor<>(rowMapper, 1), args));
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        return query(sql, new RowMapperResultSetExtractor<>(rowMapper));
    }

    private void setArguments(final PreparedStatement pstmt, final Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            log.debug("index:{}, args:{}", i + 1, args[i]);
            pstmt.setObject(i + 1, args[i]);
        }
    }

    private <T> List<T> query(String sql, RowMapperResultSetExtractor<T> rowMapper, Object... args) {
        return execute(sql, pstmt -> {
            setArguments(pstmt, args);
            ResultSet rs = pstmt.executeQuery();
            return rowMapper.extractData(rs);
        });
    }

    private <T> T execute(final String sql, PreparedStatementCallback<T> callback) {
        try {
            Connection conn = DataSourceUtils.getConnection(dataSource);
            PreparedStatement ptsmt = conn.prepareStatement(sql);
            log.debug("query : {}", sql);

            return callback.doInPreparedStatement(ptsmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }
}
