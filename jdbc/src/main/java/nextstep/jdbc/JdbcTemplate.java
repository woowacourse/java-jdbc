package nextstep.jdbc;

import nextstep.jdbc.exception.JdbcTemplateException;
import nextstep.jdbc.utils.DataAccessUtils;
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

    public void update(String sql, Object... args) {
        log.info("JdbcTemplate.update, sql: {}", sql);
        execute(sql, PreparedStatement::execute, args);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        log.info("JdbcTemplate.update, sql: {}", sql);
        return execute(sql, preparedStatement -> getResults(rowMapper, preparedStatement), args);
    }

    private <T> List<T> getResults(RowMapper<T> rowMapper, PreparedStatement preparedStatement) {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            List<T> result = new ArrayList<>();
            while (resultSet.next()) {
                result.add(rowMapper.apply(resultSet));
            }
            return result;
        } catch (SQLException e) {
            log.debug("ResultSet failed when execute query: {}", e.getMessage());
            throw new JdbcTemplateException(e.getMessage());
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        log.info("JdbcTemplate.queryForObject, query: {}", sql);
        return DataAccessUtils.singleResult(query(sql, rowMapper, args));
    }

    public void delete(String sql, Object... args) {
        log.info("JdbcTemplate.delete, sql: {}", sql);
        execute(sql, PreparedStatement::execute, args);
    }

    private <T> T execute(String sql, PreparedStatementExecutor<T> preparedStatementExecutor, Object... args) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            setParameters(preparedStatement, args);
            return preparedStatementExecutor.execute(preparedStatement);
        } catch (SQLException e) {
            log.debug("JdbcTemplate execution failed: {}", e.getMessage());
            throw new JdbcTemplateException(e.getMessage());
        }
    }

    private void setParameters(PreparedStatement preparedStatement, Object[] args) throws SQLException {
        int index = 1;
        for (Object arg : args) {
            preparedStatement.setObject(index++, arg);
        }
    }
}
