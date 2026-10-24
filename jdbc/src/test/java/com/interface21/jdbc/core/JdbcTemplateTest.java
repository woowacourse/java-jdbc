package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

  private final DataSource dataSource = mock(DataSource.class);
  private final Connection connection = mock(Connection.class);
  private final PreparedStatement preparedStatement = mock(PreparedStatement.class);
  private final ResultSet resultSet = mock(ResultSet.class);
  private final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

  @BeforeEach
  void setUp() throws SQLException {
    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
  }

  @Test
  void updatePreservesSqlExceptionAndClosesResources() throws SQLException {
    final SQLException cause = new SQLException("SQL execution failed");
    when(preparedStatement.executeUpdate()).thenThrow(cause);

    assertThatThrownBy(() -> jdbcTemplate.update(
        "update users set password = ? where id = ?", "new-password", 1L))
        .isInstanceOf(DataAccessException.class)
        .hasCause(cause);

    verify(preparedStatement).close();
    verify(connection).close();
  }

  @Test
  void queryPreservesMappingExceptionAndClosesResources() throws SQLException {
    final SQLException cause = new SQLException("Row mapping failed");
    when(preparedStatement.executeQuery()).thenReturn(resultSet);
    when(resultSet.next()).thenReturn(true);
    final RowMapper<String> rowMapper = row -> {
      throw cause;
    };

    assertThatThrownBy(() -> jdbcTemplate.query("select account from users", rowMapper))
        .isInstanceOf(DataAccessException.class)
        .hasCause(cause);

    verify(resultSet).close();
    verify(preparedStatement).close();
    verify(connection).close();
  }
}
