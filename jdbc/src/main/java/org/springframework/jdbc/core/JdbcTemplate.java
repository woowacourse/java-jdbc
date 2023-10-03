package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void executeUpdate(final String sql, final Object... parameters) {
        try (final PreparedStatement preparedStatement = dataSource.getConnection()
                .prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            setParameters(parameters, preparedStatement);
            log.debug("query : {}", preparedStatement);

            preparedStatement.executeUpdate();
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
    }

    private void setParameters(final Object[] parameters, final PreparedStatement preparedStatement) throws SQLException {
        for (int i = 1; i <= parameters.length; i++) {
            preparedStatement.setObject(i, parameters[i - 1]);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        try (final PreparedStatement preparedStatement = dataSource.getConnection()
                .prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            setParameters(parameters, preparedStatement);
            log.debug("query : {}", preparedStatement);

            final ResultSet resultSet = preparedStatement.executeQuery();
            return rowMapper.map(resultSet);
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> queryForList(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        final RowMapper<List<T>> listMapper = resultSet -> {
            final int rowNumber = getRowNumber(resultSet);

            final List<T> result = new ArrayList<>();
            for (int i = 0; i < rowNumber; i++) {
                final T entity = rowMapper.map(resultSet);
                result.add(entity);
            }
            return result;
        };

        return queryForObject(sql, listMapper, parameters);
    }

    private int getRowNumber(final ResultSet resultSet) throws SQLException {
        resultSet.last();
        final int rowNumber = resultSet.getRow();
        resultSet.beforeFirst();

        return rowNumber;
    }

}
