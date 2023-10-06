package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    private static final int FIRST = 0;
    private static final int ONE = 1;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void executeUpdate(final String sql, final Object... parameters) {
        try (final PreparedStatement preparedStatement = dataSource.getConnection().prepareStatement(sql)) {
            setParameters(parameters, preparedStatement);
            log.debug("query : {}", preparedStatement);

            preparedStatement.executeUpdate();
        } catch (final Exception e) {
            throw new DataAccessException(e);
        }
    }

    private void setParameters(final Object[] parameters, final PreparedStatement preparedStatement) throws SQLException {
        for (int i = 1; i <= parameters.length; i++) {
            preparedStatement.setObject(i, parameters[i - 1]);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        final List<T> result = query(sql, rowMapper, parameters);

        if (result.size() != ONE) {
            throw new DataAccessException("실제 결과가 1개여야 합니다.");
        }

        return result.get(FIRST);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        try (final PreparedStatement preparedStatement = dataSource.getConnection().prepareStatement(sql)) {
            setParameters(parameters, preparedStatement);
            log.debug("query : {}", preparedStatement);

            final ResultSet resultSet = preparedStatement.executeQuery();

            return mapRows(rowMapper, resultSet);
        } catch (final Exception e) {
            throw new DataAccessException(e);
        }
    }

    private <T> List<T> mapRows(final RowMapper<T> rowMapper, final ResultSet resultSet) throws SQLException {
        final List<T> result = new ArrayList<>();

        while (resultSet.next()) {
            final T tuple = rowMapper.map(resultSet);
            result.add(tuple);
        }

        return result;
    }

    public <T> List<T> queryForList(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        return query(sql, rowMapper, parameters);
    }

    public void executeUpdate(final Connection connection, final String sql, final Object... parameters) {
        try (final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setParameters(parameters, preparedStatement);
            log.debug("query : {}", preparedStatement);

            preparedStatement.executeUpdate();
        } catch (final Exception e) {
            throw new DataAccessException(e);
        }
    }

}
