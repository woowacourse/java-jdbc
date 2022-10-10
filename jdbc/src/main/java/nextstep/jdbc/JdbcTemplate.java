package nextstep.jdbc;

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
    private static final int ONE_OBJECT_ROW_NUM = 1;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, Object... params) {
        return doExecute(sql, preparedStatement -> {
            setParamsToStatement(preparedStatement, params);
            return preparedStatement.executeUpdate();
        });
    }

    public int update(final Connection connection, final String sql, Object... params) {
        return doExecute(connection, sql, preparedStatement -> {
            setParamsToStatement(preparedStatement, params);
            return preparedStatement.executeUpdate();
        });
    }

    public <T> T queryForObject(final String sql, RowMapper<T> rowMapper, Object... params) {
        return doExecute(sql, preparedStatement -> {
            setParamsToStatement(preparedStatement, params);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return mappingRowToObject(resultSet, rowMapper);
            }
        });
    }

    private <T> T mappingRowToObject(ResultSet resultSet, RowMapper<T> rowMapper) throws SQLException {
        if (!resultSet.next()) {
            throw new DataAccessException("매칭되는 결과값이 없습니다.");
        }

        final T mappingObject = rowMapper.mapRow(resultSet, ONE_OBJECT_ROW_NUM);
        if (resultSet.next()) {
            throw new DataAccessException("행이 여러개가 있습니다.");
        }

        return mappingObject;
    }

    public <T> List<T> query(final String sql, RowMapper<T> rowMapper, Object... params) {
        return doExecute(sql, preparedStatement -> {
            setParamsToStatement(preparedStatement, params);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return mappingRowToObjects(resultSet, rowMapper);
            }
        });
    }

    private <T> List<T> mappingRowToObjects(ResultSet resultSet, RowMapper<T> rowMapper)
            throws SQLException {
        final List<T> objects = new ArrayList<>();
        int rowNum = 1;
        while (resultSet.next()) {
            objects.add(rowMapper.mapRow(resultSet, rowNum++));
        }

        return objects;
    }

    private void setParamsToStatement(PreparedStatement preparedStatement, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            preparedStatement.setObject(i + 1, params[i]);
        }
    }

    private <T> T doExecute(String sql, PreparedStatementCallback<T> callback) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            return callback.doInPreparedStatement(preparedStatement);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private <T> T doExecute(Connection connection, String sql, PreparedStatementCallback<T> callback) {
        try (
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            return callback.doInPreparedStatement(preparedStatement);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
