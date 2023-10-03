package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final ConnectionTemplate connectionTemplate;

    public JdbcTemplate(final DataSource dataSource) {
        this.connectionTemplate = new ConnectionTemplate(dataSource);
    }

    public <T> List<T> query(final String sql,
                             final RowMapper<T> rowMapper,
                             final Object... arguments) {
        return connectionTemplate.query(sql, preparedStatement -> {
            final ResultSet resultSet = preparedStatement.executeQuery();
            final List<T> list = new ArrayList<>();
            while (resultSet.next()) {
                list.add(rowMapper.mapRow(resultSet));
            }
            return list;
        }, arguments);
    }

    public <T> Optional<T> querySingleRow(final String sql,
                                          final RowMapper<T> rowMapper,
                                          final Object... arguments) {
        return connectionTemplate.query(sql, preparedStatement -> {
            final ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(rowMapper.mapRow(resultSet));
            }
            return Optional.empty();
        }, arguments);
    }

    public void update(final String sql,
                       final Object... arguments) {
        connectionTemplate.query(sql, PreparedStatement::executeUpdate, arguments);
    }
}
