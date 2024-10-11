package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper) {
        return queryForObject(sql, rowMapper, new DefaultPreparedStatementSetter());
    }

    public <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, Object... parameters) {
        return queryForObject(sql, rowMapper, new ParameterPreparedStatementSetter(parameters));
    }

    public <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, PreparedStatementSetter pstmtSetter) {
        List<T> results = query(sql, rowMapper, pstmtSetter);
        if (results.isEmpty()) {
            return Optional.empty();
        }
        if (results.size() == 1) {
            return Optional.of(results.getFirst());
        }
        throw new NotSingleResultDataAccessException();
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        return query(sql, rowMapper, new DefaultPreparedStatementSetter());
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... parameters) {
        return query(sql, rowMapper, new ParameterPreparedStatementSetter(parameters));
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, PreparedStatementSetter pstmtSetter) {
        return preparePreparedStatement(pstmt -> createResult(pstmt, rowMapper), sql, pstmtSetter);
    }

    private <T> List<T> createResult(PreparedStatement pstmt, RowMapper<T> rowMapper) throws SQLException {
        try (ResultSet resultSet = pstmt.executeQuery()) {
            List<T> result = new ArrayList<>();
            while (resultSet.next()) {
                result.add(rowMapper.mapRow(resultSet));
            }
            return result;
        }
    }

    public void update(String sql) {
        update(sql, new DefaultPreparedStatementSetter());
    }

    public void update(String sql, Object... parameters) {
        update(sql, new ParameterPreparedStatementSetter(parameters));
    }

    public void update(String sql, PreparedStatementSetter pstmtsetter) {
        preparePreparedStatement(PreparedStatement::executeUpdate, sql, pstmtsetter);
    }

    private <T> T preparePreparedStatement(JdbcRunner<T> jdbcRunner, String sql, PreparedStatementSetter pstmtSetter) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = createPreparedStatement(connection, sql, pstmtSetter)) {
            log.debug("query = {}", sql);
            return jdbcRunner.run(pstmt);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private PreparedStatement createPreparedStatement(Connection connection,
                                                      String sql,
                                                      PreparedStatementSetter pstmtSetter) throws SQLException {
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmtSetter.setValue(pstmt);
        return pstmt;
    }

    @FunctionalInterface
    private interface JdbcRunner<T> {

        T run(PreparedStatement pstmt) throws SQLException;
    }
}
