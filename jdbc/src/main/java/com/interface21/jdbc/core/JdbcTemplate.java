package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
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
    try (Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      bindParameters(preparedStatement, parameters);
      log.debug("query : {}", sql);
      return preparedStatement.executeUpdate();
    } catch (SQLException exception) {
      throw new DataAccessException(exception);
    }
  }

  public <T> List<T> query(
      final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      bindParameters(preparedStatement, parameters);
      log.debug("query : {}", sql);

      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        final List<T> results = new ArrayList<>();
        while (resultSet.next()) {
          results.add(rowMapper.mapRow(resultSet));
        }
        return results;
      }
    } catch (SQLException exception) {
      throw new DataAccessException(exception);
    }
  }

  public <T> T queryForObject(
      final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      bindParameters(preparedStatement, parameters);
      log.debug("query : {}", sql);

      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (!resultSet.next()) {
          return null;
        }
        final T result = rowMapper.mapRow(resultSet);
        if (resultSet.next()) {
          throw new DataAccessException("Expected at most one row");
        }
        return result;
      }
    } catch (SQLException exception) {
      throw new DataAccessException(exception);
    }
  }

  private void bindParameters(
      final PreparedStatement preparedStatement, final Object[] parameters) throws SQLException {
    for (int index = 0; index < parameters.length; index++) {
      preparedStatement.setObject(index + 1, parameters[index]);
    }
  }
}
