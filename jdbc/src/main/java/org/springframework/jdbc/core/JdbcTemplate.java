package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

  private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
  private static final int SINGLE_RESULT_VALUE = 1;

  private final DataSource dataSource;

  public JdbcTemplate(final DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public <T> Optional<T> query(final String sql, RowMapper<T> rowMapper, final Object... values) {

    try (
        final Connection conn = dataSource.getConnection();
        final PreparedStatement pstmt = conn.prepareStatement(sql)
    ) {

      for (int index = 1; index <= values.length; index++) {
        pstmt.setObject(index, values[index - 1]);
      }

      final ResultSet resultSet = pstmt.executeQuery();

      final List<T> result = extractResultFrom(resultSet, rowMapper);

      validateResultSizeIsSingle(result);

      return Optional.ofNullable(result.get(0));
    } catch (SQLException e) {
      log.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  private <T> void validateResultSizeIsSingle(final List<T> result) {
    if (result.size() > SINGLE_RESULT_VALUE) {
      throw new IllegalArgumentException("값이 2개 이상입니다.");
    }
  }

  public <T> List<T> queryPlural(
      final String sql,
      final RowMapper<T> rowMapper,
      final Object... values
  ) {
    try (
        final Connection conn = dataSource.getConnection();
        final PreparedStatement pstmt = conn.prepareStatement(sql)
    ) {

      for (int index = 1; index <= values.length; index++) {
        pstmt.setObject(index, values[index - 1]);
      }

      final ResultSet resultSet = pstmt.executeQuery();

      return extractResultFrom(resultSet, rowMapper);
    } catch (SQLException e) {
      log.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  private <T> List<T> extractResultFrom(final ResultSet resultSet, final RowMapper<T> rowMapper)
      throws SQLException {
    final List<T> results = new ArrayList<>();

    while (resultSet.next()) {
      results.add(rowMapper.map(resultSet));
    }

    return results;
  }

  public void execute(final String sql, final Object... values) {
    try (
        final Connection conn = dataSource.getConnection();
        final PreparedStatement pstmt = conn.prepareStatement(sql)
    ) {

      for (int index = 1; index <= values.length; index++) {
        pstmt.setObject(index, values[index - 1]);
      }

      pstmt.executeUpdate();
    } catch (SQLException e) {
      log.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }
}
