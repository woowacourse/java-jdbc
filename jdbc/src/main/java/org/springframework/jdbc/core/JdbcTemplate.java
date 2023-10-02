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

  public <T> T query(final String sql, RowMapper<T> rowMapper, final Object... values) {

    try (
        final Connection conn = dataSource.getConnection();
        final PreparedStatement pstmt = conn.prepareStatement(sql)
    ) {

      for (int index = 1; index <= values.length; index++) {
        pstmt.setObject(index, values[index - 1]);
      }

      final ResultSet resultSet = pstmt.executeQuery();

      if (resultSet.next()) {
        return rowMapper.map(resultSet);
      }

    } catch (SQLException e) {
      log.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }

    return null;
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
      final List<T> results = new ArrayList<>();

      while (resultSet.next()) {
        results.add(rowMapper.map(resultSet));
      }

      return results;
    } catch (SQLException e) {
      log.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
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
