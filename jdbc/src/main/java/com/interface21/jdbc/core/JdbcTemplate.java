package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.interface21.dao.DataAccessException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... args) {
        execute(sql, args, PreparedStatement::executeUpdate);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return execute(sql, args, statement -> {
            final ResultSet resultSet = statement.executeQuery();
            SingleDataExtractor singleDataExtractor = new SingleDataExtractor();
            return singleDataExtractor.extract(resultSet, rowMapper)
                    .getFirst();
        });
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return execute(sql, args, statement -> {
            final ResultSet resultSet = statement.executeQuery();
            MultiDataExtractor multiDataExtractor = new MultiDataExtractor();
            return multiDataExtractor.extract(resultSet, rowMapper);
        });
    }

    private <T> T execute(final String sql, final Object[] args, final QueryExecutor<T> executor) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = connection.prepareStatement(sql)) {
            setArguments(args, statement);
            log.info("query : {}", sql);
            return executor.execute(statement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void setArguments(final Object[] args, final PreparedStatement statement) throws SQLException {
        int index = 1;
        for (final Object arg : args) {
            statement.setObject(index++, arg);
        }
    }
}
