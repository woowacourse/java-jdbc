package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(QueryConnectionHolder queryConnectionHolder, PreparedStatementSetter preparedStatementSetter) {
        try {
            PreparedStatement preparedStatement = queryConnectionHolder.getAsPreparedStatement();
            preparedStatementSetter.setValues(preparedStatement);
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Update 실패", e);
        }
    }

    public int update(Connection connection, String sql, Object... args) {
        return update(new QueryConnectionHolder(connection, sql), new PreparedStatementArgumentsSetter(args));
    }

    public int update(String sql, PreparedStatementSetter psSetter) {
        try (Connection connection = dataSource.getConnection()) {
             return update(new QueryConnectionHolder(connection, sql), psSetter);
        } catch (SQLException e) {
            throw new DataAccessException("Update 실패", e);
        }
    }

    public int update(String sql, Object... args) {
        return update(sql, new PreparedStatementArgumentsSetter(args));
    }

    public <T> List<T> query(String sql, PreparedStatementSetter psSetter, RowMapper<T> rowMapper) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            psSetter.setValues(ps);
            return retrieveRow(rowMapper, ps);
        } catch (SQLException e) {
            throw new DataAccessException("Query 실패", e);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        return query(sql, new PreparedStatementArgumentsSetter(args), rowMapper);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        List<T> query = query(sql, rowMapper, args);
        return query.isEmpty() ? null : query.getLast();
    }

    private <T> List<T> retrieveRow(RowMapper<T> rowMapper, PreparedStatement ps) throws SQLException {
        List<T> results = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                results.add(rowMapper.mapRow(rs));
            }
        }
        return results;
    }
}
