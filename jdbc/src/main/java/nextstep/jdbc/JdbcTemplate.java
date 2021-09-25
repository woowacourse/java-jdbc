package nextstep.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        log.debug("jdbcTemplate queryForObject method - query : " + sql);
        return execute(sql, args, pstmt -> {
            try (ResultSet rs = pstmt.executeQuery()) {
                T result = null;
                while (rs.next()) {
                    result = rowMapper.mapRow(rs);
                }
                return result;
            }
        });
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        log.debug("jdbcTemplate query method - query : " + sql);
        return execute(sql, args, pstmt -> {
            List<T> results = new ArrayList<>();
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs));
                }
                return results;
            }
        });
    }

    public void update(String sql, Object... args) {
        log.debug("jdbcTemplate update method - query : " + sql);
        execute(sql, args, PreparedStatement::executeUpdate);
    }

    private <T> T execute(String sql, Object[] args, JdbcPreparedStatementExecution<T> execution) {
        log.debug("jdbcTemplate execute method");

        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            setStatementArguments(pstmt, args);
            return execution.execute(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage());
            throw new JdbcExecutionException(e.getMessage());
        }
    }

    private void setStatementArguments(PreparedStatement pstmt, Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }
}
