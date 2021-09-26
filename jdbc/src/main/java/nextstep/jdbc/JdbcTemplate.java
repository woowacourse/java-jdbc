package nextstep.jdbc;

import nextstep.jdbc.exception.JdbcTemplateException;
import nextstep.jdbc.utils.DataAccessUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
        return execute(sql, preparedStatement -> {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                List<T> result = new ArrayList<>();
                while (resultSet.next()) {
                    result.add(rowMapper.apply(resultSet));
                }
                return result;
            } catch (Exception e) {
                log.debug("ResultSet failed when execute query: {}", e.getMessage());
                throw new JdbcTemplateException(e.getMessage());
            }
        }, args);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        log.info("JdbcTemplate.queryForObject, query: {}", sql);
        return DataAccessUtils.singleResult(query(sql, rowMapper, args));
    }

    private <T> T execute(String sql, PreparedStatementExecutor<T> preparedStatementExecutor, Object... args) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            int index = 1;
            for (Object arg : args) {
                preparedStatement.setObject(index++, arg);
            }
            return preparedStatementExecutor.execute(preparedStatement);
        } catch (Exception e) {
            log.debug("JdbcTemplate execution failed: {}", e.getMessage());
            throw new JdbcTemplateException(e.getMessage());
        }
    }
}
