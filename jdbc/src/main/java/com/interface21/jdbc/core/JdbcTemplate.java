package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
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

  public int update(final String sql, final Object... parameters) {
    return execute(sql, parameters, PreparedStatement::executeUpdate);
  }

  public <T> List<T> query(
      final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
    return execute(sql, parameters, preparedStatement -> {
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        final List<T> results = new ArrayList<>();
        while (resultSet.next()) {
          results.add(rowMapper.mapRow(resultSet));
        }
        return results;
      }
    });
  }

  /** 조회 결과가 없으면 null을 반환하고, 두 건 이상이면 DataAccessException을 던진다. */
  public <T> T queryForObject(
      final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
    return execute(sql, parameters,
        preparedStatement -> queryForSingleRow(preparedStatement, sql, rowMapper));
  }

  private <T> T queryForSingleRow(
      final PreparedStatement preparedStatement,
      final String sql,
      final RowMapper<T> rowMapper) throws SQLException {
    try (ResultSet resultSet = preparedStatement.executeQuery()) {
      if (!resultSet.next()) {
        return null;
      }
      final T result = rowMapper.mapRow(resultSet);
      if (resultSet.next()) {
        throw new DataAccessException("Expected at most one row for SQL: " + sql);
      }
      return result;
    }
  }

  private <T> T execute(
      final String sql,
      final Object[] parameters,
      final PreparedStatementCallback<T> callback) {
    final Connection connection = DataSourceUtils.getConnection(dataSource);
    Throwable originalException = null;
    try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      bindParameters(preparedStatement, parameters);
      log.debug("query : {}", sql);
      return callback.execute(preparedStatement);
    } catch (SQLException exception) {
      final DataAccessException dataAccessException =
          new DataAccessException("Failed to execute SQL: " + sql, exception);
      originalException = dataAccessException;
      throw dataAccessException;
    } catch (RuntimeException | Error exception) {
      originalException = exception;
      throw exception;
    } finally {
      DataSourceUtils.releaseConnection(connection, dataSource, originalException);
    }
  }

  private void bindParameters(
      final PreparedStatement preparedStatement, final Object[] parameters) throws SQLException {
    for (int index = 0; index < parameters.length; index++) {
      preparedStatement.setObject(index + 1, parameters[index]);
    }
  }

  @FunctionalInterface
  private interface PreparedStatementCallback<T> {

    T execute(PreparedStatement preparedStatement) throws SQLException;
  }
}
