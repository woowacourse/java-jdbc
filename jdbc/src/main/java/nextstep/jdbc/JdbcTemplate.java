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

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object... args) {
        execute(sql, PreparedStatement::executeUpdate, args);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        List<T> results = query(sql, rowMapper, args);
        return DataAccessUtils.nullableSingleResult(results);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        return execute(sql, preparedStatement -> getResult(preparedStatement, rowMapper), args);
    }

    private <T> T execute(String sql, PreparedStatementCallback<T> action, Object... args) {
        log.debug("Execute sql : {}", sql);

        Connection connection = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setParameters(args, preparedStatement);
            return action.doInPreparedStatement(preparedStatement);
        } catch (SQLException e) {
            throw new DataAccessException();
        }
    }

    private void setParameters(Object[] args, PreparedStatement preparedStatement) throws SQLException {
        int index = 1;
        for (Object arg : args) {
            preparedStatement.setObject(index++, arg);
        }
    }

    private <T> List<T> getResult(PreparedStatement preparedStatement, RowMapper<T> rowMapper) throws SQLException {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            List<T> result = new ArrayList<>();
            int rowNum = 0;
            while (resultSet.next()) {
                result.add(rowMapper.mapRow(resultSet, rowNum++));
            }
            return result;
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
