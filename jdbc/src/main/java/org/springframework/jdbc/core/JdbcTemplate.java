package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

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

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... args) {
        execute(sql, args, PreparedStatement::executeUpdate);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return execute(sql, args, (PreparedStatement pstmt) -> {
            ResultSet resultSet = pstmt.executeQuery();
            if (resultSet.next()) {
                return rowMapper.mapRow(resultSet);
            }
            throw new RuntimeException();
        });
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return execute(sql, args, (PreparedStatement pstmt) -> {
            ResultSet resultSet = pstmt.executeQuery();
            final List<T> result = new ArrayList<>();
            while (resultSet.next()) {
                result.add(rowMapper.mapRow(resultSet));
            }
            return result;
        });
    }

    private <T> T execute(final String sql, final Object[] args, final PreparedStatementFunction<T> preparedStatementExecutor) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
            return preparedStatementExecutor.execute(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }
}
