package org.springframework.jdbc.core;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTemplate {

    private final ConnectionTemplate connectionTemplate;

    public JdbcTemplate(final DataSource dataSource) {
        this.connectionTemplate = new ConnectionTemplate(dataSource);
    }

    public <T> List<T> query(final String sql,
                             final RowMapper<T> rowMapper,
                             final Object... arguments) {
        return connectionTemplate.readResult(sql, resultSet -> {
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
        return connectionTemplate.readResult(sql, resultSet -> {
            if (resultSet.next()) {
                return Optional.of(rowMapper.mapRow(resultSet));
            }
            return Optional.empty();
        }, arguments);
    }

    public void update(final String sql,
                       final Object... arguments) {
        connectionTemplate.update(sql, PreparedStatement::executeUpdate, arguments);
    }
}
