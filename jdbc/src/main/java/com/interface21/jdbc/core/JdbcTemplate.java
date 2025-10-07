package com.interface21.jdbc.core;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... parameters) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);

            bindParameters(preparedStatement, parameters);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        final List<T> result = query(sql, rowMapper, parameters);

        if (result.isEmpty()) {
            return null;
        }
        return result.getFirst();
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);

            bindParameters(preparedStatement, parameters);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                return extractResults(resultSet, rowMapper);
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void bindParameters(final PreparedStatement preparedStatement, final Object[] parameters) {
        for (int i = 0; i < parameters.length; i++) {
            final Object parameter = parameters[i];
            bindParameter(preparedStatement, parameter, i + 1);
        }
    }

    private void bindParameter(final PreparedStatement preparedStatement, final Object parameter, final int index) {
        final String simpleName = parameter.getClass().getSimpleName();

        try {
            final Method method = Arrays.stream(preparedStatement.getClass().getMethods())
                    .filter(m -> m.getName().startsWith("set"))
                    .filter(m -> m.getName().toLowerCase().contains(simpleName.toLowerCase()))
                    .findFirst()
                    .orElse(preparedStatement.getClass().getMethod("setObject", int.class, Object.class));

            method.invoke(preparedStatement, index, parameter);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    private <T> List<T> extractResults(final ResultSet resultSet, final RowMapper<T> rowMapper) throws SQLException {
        final List<T> results = new ArrayList<>();

        int rowNumber = 0;
        while (resultSet.next()) {
            results.add(rowMapper.mapRow(resultSet, rowNumber++));
        }

        return results;
    }
}
