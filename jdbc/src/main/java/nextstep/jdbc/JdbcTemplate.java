package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            log.info("query : {}", sql);
            return convertObjects(rowMapper, statement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private <T> List<T> convertObjects(RowMapper<T> rowMapper, PreparedStatement preparedStatement) throws SQLException {
        List<T> objects = new ArrayList<>();
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            T object = rowMapper.mapRow(resultSet);
            objects.add(object);
        }
        resultSet.close();
        return objects;
    }

    public <T> T queryForObject(String sql, Map<Integer, Object> parameters, RowMapper<T> rowMapper) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            log.info("query : {}", sql);
            setPreparedStatement(preparedStatement, parameters);
            return convertObject(preparedStatement, rowMapper);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

    private <T> T convertObject(PreparedStatement preparedStatement, RowMapper<T> rowMapper) throws SQLException {
        T object = null;
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            object = rowMapper.mapRow(resultSet);
        }
        resultSet.close();
        return object;
    }

    public void updateQuery(String sql) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            log.info("query : {}", sql);
            statement.execute();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            e.printStackTrace();
        }
    }

    public void updateQuery(String sql, Map<Integer, Object> parameters) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            log.info("query : {}", sql);
            setPreparedStatement(preparedStatement, parameters);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private void setPreparedStatement(PreparedStatement preparedStatement, Map<Integer, Object> parameters)
            throws SQLException {
        for (Entry<Integer, Object> integerObjectEntry : parameters.entrySet()) {
            preparedStatement.setObject(integerObjectEntry.getKey(), integerObjectEntry.getValue());
        }
    }
}
