package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.EmptyResultDataAccessException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
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

    public void update(String sql, Object... params) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            bindStatementParameters(preparedStatement, params);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> query(String sql, ResultSetMapper<T> resultSetMapper, Object... params) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            bindStatementParameters(preparedStatement, params);
            ResultSet resultSet = preparedStatement.executeQuery();
            List<T> queriedData = new ArrayList<>();
            while (resultSet.next()) {
                queriedData.add(resultSetMapper.map(resultSet));
            }
            return queriedData;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> T queryForObject(String sql, ResultSetMapper<T> resultSetMapper, Object... params) {
        List<T> queriedData = query(sql, resultSetMapper, params);
        if (queriedData.size() < 1) {
            throw new EmptyResultDataAccessException();
        } else if (queriedData.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(1, queriedData.size());
        }
        return queriedData.get(0);
    }

    private void bindStatementParameters(PreparedStatement preparedStatement, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            preparedStatement.setObject(i + 1, params[i]);
        }
    }
}
