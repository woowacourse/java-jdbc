package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
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

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, Object... args) {
        Integer result = execute(sql, PreparedStatement::executeUpdate, args);
        return Optional.ofNullable(result).orElse(0);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        return execute(sql, statement -> {
            ArrayList<T> results = new ArrayList<>();
            ResultSet resultSet = statement.executeQuery();

            for (int rowNum = 0; resultSet.next(); rowNum++) {
                results.add(rowMapper.mapRow(resultSet, rowNum));
            }

            return Collections.unmodifiableList(results);
        }, args);
    }

    public <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        return execute(sql, statement -> {
            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                return Optional.empty();
            }

            return Optional.of(rowMapper.mapRow(resultSet, 0));
        }, args);
    }

    private <T> T execute(String sql, SqlExecutor<T> executor, Object... args) {
        log.debug("query : {}", sql);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            if (args == null) {
                return executor.execute(statement);
            }
            for (int i = 0; i < args.length; i++) {
                statement.setObject(i + 1, args[i]);
            }
            return executor.execute(statement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }
}
