package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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

    public void execute(final String sql, final Object... elements) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            setElements(elements, preparedStatement);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void setElements(final Object[] elements, final PreparedStatement preparedStatement) throws SQLException {
        for (int i = 0; i < elements.length; i += 1) {
            preparedStatement.setObject(i + 1, elements[i]);
        }
    }

    public <T> T queryForObject(
            final RowMapper<T> rowMapper,
            final String sql,
            final Object... elements
    ) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            setElements(elements, preparedStatement);
            final ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return rowMapper.mapRow(resultSet);
            }
            return null;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> query(
            final RowMapper<T> rowMapper,
            final String sql,
            final Object... elements
    ) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            setElements(elements, preparedStatement);
            final ResultSet resultSet = preparedStatement.executeQuery();

            final List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet));
            }
            return results;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

}
