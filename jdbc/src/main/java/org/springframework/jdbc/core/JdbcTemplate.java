package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.exception.IncorrectResultSizeDataAccessException;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final JdbcExecutor executor;

    public JdbcTemplate(final DataSource dataSource) {
        this.executor = new JdbcExecutor(dataSource);
    }

    public int update(final String sql, final Object... args) {
        final Function<PreparedStatement, Integer> fuction = pstmt -> {
            try {
                return pstmt.executeUpdate();
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        };

        return executor.execute(sql, args, fuction);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final Function<PreparedStatement, List<T>> function = pstmt -> {
            try {
                final List<T> results = new ArrayList<>();
                final ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs));
                }

                return results;
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        };

        return executor.execute(sql, args, function);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final Function<PreparedStatement, T> function = pstmt -> {
            try {
                final ResultSet rs = pstmt.executeQuery();
                T findObject = null;
                if (rs.next()) {
                    findObject = rowMapper.mapRow(rs);
                }

                validateSingleResult(findObject, rs);

                return findObject;
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        };

        return executor.execute(sql, args, function);
    }

    private <T> void validateSingleResult(final T findObject, final ResultSet rs) throws SQLException {
        if (findObject == null) {
            throw new IncorrectResultSizeDataAccessException(0);
        }
        if (rs.next()) {
            rs.last();

            throw new IncorrectResultSizeDataAccessException(rs.getRow());
        }
    }
}
