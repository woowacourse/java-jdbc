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
import org.springframework.jdbc.datasource.DataSourceUtils;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(String sql, Object... params) {
        execute(sql, PreparedStatement::executeUpdate, params);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... params) {
        return execute(sql, preparedStatement -> getObjects(preparedStatement, rowMapper), params);
    }

    private <T> List<T> getObjects(PreparedStatement preparedStatement, RowMapper<T> rowMapper) throws SQLException {
        ResultSet resultSet = preparedStatement.executeQuery();
        List<T> objects = new ArrayList<>();
        for (int rowNumber = 0; resultSet.next(); rowNumber++) {
            objects.add(rowMapper.mapRow(resultSet, rowNumber));
        }
        return objects;

    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        List<T> results = query(sql, rowMapper, params);
        return extractObject(results);
    }

    private <T> T extractObject(List<T> result) {
        if (result.size() != 1) {
            throw new IncorrectResultSizeDataAccessException();
        }
        return result.get(0);
    }

    private <T> T execute(String sql, PreparedStatementCallback<T> callback, Object... params) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ) {
            setParams(preparedStatement, params);
            return callback.execute(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void setParams(PreparedStatement preparedStatement, Object... params) throws SQLException {
        int index = 1;
        for (Object param : params) {
            preparedStatement.setObject(index++, param);
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}

