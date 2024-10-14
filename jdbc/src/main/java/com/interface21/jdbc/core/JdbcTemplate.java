package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    private static final int INIT_ROW_NUMBER = 0;
    private static final int REQUIRED_DATA_SIZE = 1;
    public static final String DATABASE_CONNECTION_ERROR = "데이터베이스 연결에 실패했습니다. : %s";

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, PreparedStatementSetter preparedStatementSetter) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            int rows = executeUpdate(preparedStatementSetter, statement);
            connection.commit();
            return rows;
        } catch (SQLException e) {
            log.error("error : {}", e.getMessage(), e);
            throw new DataAccessException(String.format(DATABASE_CONNECTION_ERROR, e.getMessage()));
        }
    }

    public void update(Connection connection, String sql, PreparedStatementSetter preparedStatementSetter) {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            executeUpdate(preparedStatementSetter, statement);
        } catch (SQLException e) {
            log.error("error : {}", e.getMessage(), e);
            throw new DataAccessException(String.format(DATABASE_CONNECTION_ERROR, e.getMessage()));
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, PreparedStatementSetter preparedStatementSetter) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            preparedStatementSetter.setValues(statement);
            ResultSet resultSet = statement.executeQuery();
            return extractData(resultSet, rowMapper);
        } catch (SQLException e) {
            log.error("error : {}", e.getMessage(), e);
            throw new DataAccessException(String.format(DATABASE_CONNECTION_ERROR, e.getMessage()));
        }
    }

    public <T> List<T> query(Connection connection, String sql, RowMapper<T> rowMapper, PreparedStatementSetter preparedStatementSetter) {
        try (PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            preparedStatementSetter.setValues(statement);
            ResultSet resultSet = statement.executeQuery();
            return extractData(resultSet, rowMapper);
        } catch (SQLException e) {
            log.error("error : {}", e.getMessage(), e);
            throw new DataAccessException(String.format(DATABASE_CONNECTION_ERROR, e.getMessage()));
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, PreparedStatementSetter preparedStatementSetter) {
        List<T> data = query(sql, rowMapper, preparedStatementSetter);
        return makeSingleResult(data);
    }

    public <T> T queryForObject(Connection connection, String sql, RowMapper<T> rowMapper, PreparedStatementSetter preparedStatementSetter) {
        List<T> data = query(connection, sql, rowMapper, preparedStatementSetter);
        return makeSingleResult(data);
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    private static int executeUpdate(PreparedStatementSetter preparedStatementSetter, PreparedStatement statement) throws SQLException {
        preparedStatementSetter.setValues(statement);
        return statement.executeUpdate();
    }

    private <T> List<T> extractData(ResultSet resultSet, RowMapper<T> rowMapper) throws SQLException {
        List<T> results = new ArrayList<>();
        int rowNumber = INIT_ROW_NUMBER;

        while (resultSet.next()) {
            results.add(rowMapper.mapRow(resultSet, rowNumber++));
        }

        return results;
    }

    private <T> T makeSingleResult(Collection<T> data) {
        if (data.isEmpty()) {
            return null;
        }
        if (data.size() > REQUIRED_DATA_SIZE) {
            throw new IllegalArgumentException("데이터가 여러개 존재합니다.");
        }
        return data.iterator().next();
    }
}
