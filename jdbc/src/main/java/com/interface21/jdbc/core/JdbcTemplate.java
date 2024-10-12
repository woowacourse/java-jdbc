package com.interface21.jdbc.core;

import com.interface21.dao.IncorrectResultSizeDataAccessException;
import com.interface21.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final SQLExceptionTranslator sqlExceptionTranslator;
    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.sqlExceptionTranslator = new SQLErrorCodeSQLExceptionTranslator();
        this.dataSource = dataSource;
    }

    public int update(String sql, Object... args) {
        Integer result = execute(sql, PreparedStatement::executeUpdate, args);
        return Optional.ofNullable(result).orElse(0);
    }

    public int update(String sql, Connection connection, Object... args) {
        Integer result = execute(sql, PreparedStatement::executeUpdate, connection, args);
        return Optional.ofNullable(result).orElse(0);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        return execute(sql, statement -> {
            List<T> results = new ArrayList<>();
            ResultSet resultSet = statement.executeQuery();

            for (int rowNum = 0; resultSet.next(); rowNum++) {
                results.add(rowMapper.mapRow(resultSet, rowNum));
            }

            return Collections.unmodifiableList(results);
        }, args);
    }

    public <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, Object... args)
            throws IncorrectResultSizeDataAccessException {
        List<T> results = query(sql, rowMapper, args);
        if (results.isEmpty()) {
            return Optional.empty();
        }
        if (results.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(1, results.size());
        }

        return Optional.of(results.getFirst());
    }

    private <T> T execute(String sql, SqlExecutor<T> executor, Object... args) {
        try (Connection connection = dataSource.getConnection();) {
            return execute(sql, executor, connection, args);
        } catch (SQLException e) {
            throw sqlExceptionTranslator.translate("execute", sql, e);
        }
    }

    private <T> T execute(String sql, SqlExecutor<T> executor, Connection connection, Object... args) {
        log.debug("query : {}", sql);

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            assignArgsToStatement(args, statement);
            return executor.execute(statement);
        } catch (SQLException e) {
            throw sqlExceptionTranslator.translate("execute", sql, e);
        }
    }

    private void assignArgsToStatement(Object[] args, PreparedStatement statement) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            statement.setObject(i + 1, args[i]);
        }
    }
}
