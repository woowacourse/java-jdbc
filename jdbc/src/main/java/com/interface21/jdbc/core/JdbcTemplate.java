package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object... args) {
        SqlParameterValidator.validate(sql, args);
        execute(sql, PreparedStatement::executeUpdate, args);
    }

    public void update(Connection conn, String sql, Object... args) {
        SqlParameterValidator.validate(sql, args);
        executeWithConnection(conn, sql, PreparedStatement::executeUpdate, args);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        SqlParameterValidator.validate(sql, args);
        return execute(
                sql,
                pstmt -> {
                    try (ResultSet rs = pstmt.executeQuery()) {
                        List<T> results = new ArrayList<>();
                        while (rs.next()) {
                            results.add(rowMapper.mapRow(rs));
                        }
                        return results;
                    }
                },
                args
        );
    }

    public <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        SqlParameterValidator.validate(sql, args);

        return execute(sql,
                pstmt -> {
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            T result = rowMapper.mapRow(rs);
                            if (rs.next()) {
                                throw new DataAccessException("결과가 2개 이상입니다.");
                            }
                            return Optional.of(result);
                        }
                    }
                    return Optional.empty();
                },
                args
        );

    }

    private <T> T execute(String sql, PrepareStatementCallback<T> action, Object... args) {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = prepareStatement(conn, sql, args)
        ) {
            return action.execute(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> T executeWithConnection(Connection conn, String sql, PrepareStatementCallback<T> action, Object... args) {
        try (PreparedStatement pstmt = prepareStatement(conn, sql, args)) {
            return action.execute(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private PreparedStatement prepareStatement(Connection conn, String sql, Object... args)
            throws SQLException {
        final PreparedStatement prepareStatement = conn.prepareStatement(sql);
        for (int i = 0; i < args.length; i++) {
            prepareStatement.setObject(i + 1, args[i]);
        }
        return prepareStatement;
    }

}
