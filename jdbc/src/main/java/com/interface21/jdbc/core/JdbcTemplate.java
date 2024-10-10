package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;

public class JdbcTemplate {

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, PreparedStatementSetter preparedStatementSetter) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            preparedStatementSetter.setValues(ps);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public void update(PreparedStatementCreator preparedStatementCreator,
                       PreparedStatementSetter preparedStatementSetter) {
        try {
            PreparedStatement preparedStatement = preparedStatementCreator.create();
            preparedStatementSetter.setValues(preparedStatement);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public int update(String sql, Object... args) {
        return update(sql, new ArgumentsPreparedStatementSetter(args));
    }

    public <T> T query(PreparedStatement preparedStatement, ResultSetExtractor<T> resultSetExtractor) {
        try (PreparedStatement ps = preparedStatement;
             ResultSet rs = ps.executeQuery()) {
            return resultSetExtractor.extractData(rs);
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public <T> T query(String sql,
                       PreparedStatementSetter preparedStatementSetter,
                       ResultSetExtractor<T> resultSetExtractor) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            preparedStatementSetter.setValues(ps);
            return query(ps, resultSetExtractor);
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        return query(sql, new ArgumentsPreparedStatementSetter(args), new RowMapperResultSetExtractor<>(rowMapper));
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        List<T> results = query(sql, rowMapper, args);
        if (results.size() != 1) {
            throw new DataSizeMismatchException(1, results.size());
        }
        return results.getFirst();
    }
}
