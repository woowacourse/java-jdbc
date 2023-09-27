package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

  private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

  private final DataSource dataSource;

  public JdbcTemplate(final DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public Map<Integer, Object> query(final String sql, final Object... values) {

    try (final Connection conn = dataSource.getConnection();
        final PreparedStatement pstmt = conn.prepareStatement(sql)) {

      for (int index = 1; index <= values.length; index++) {
        pstmt.setObject(index, values[index - 1]);
      }

      final ResultSet resultSet = pstmt.executeQuery();

      final Map<Integer, Object> result = new HashMap<>();

      if (resultSet.next()) {
        result.put(1, resultSet.getLong(1));
        result.put(2, resultSet.getString(2));
        result.put(3, resultSet.getString(3));
        result.put(4, resultSet.getString(4));
      }

      return result;
    } catch (SQLException e) {
      log.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  public List<Map<Integer, Object>> queryPlural(final String sql, final Object... values) {
    try (final Connection conn = dataSource.getConnection();
        final PreparedStatement pstmt = conn.prepareStatement(sql)) {

      for (int index = 1; index <= values.length; index++) {
        pstmt.setObject(index, values[index - 1]);
      }

      final ResultSet resultSet = pstmt.executeQuery();

      final List<Map<Integer, Object>> result = new ArrayList<>();

      int index = 0;
      while (resultSet.next()) {
        result.add(new HashMap<>());

        result.get(index).put(1, resultSet.getLong(1));
        result.get(index).put(2, resultSet.getString(2));
        result.get(index).put(3, resultSet.getString(3));
        result.get(index).put(4, resultSet.getString(4));

        index++;
      }

      return result;
    } catch (SQLException e) {
      log.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }
}
