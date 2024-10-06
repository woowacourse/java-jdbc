package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class JdbcTemplateTest {

    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        DataSource dataSource = Mockito.mock(DataSource.class);
        connection = Mockito.mock(Connection.class);
        preparedStatement = Mockito.mock(PreparedStatement.class);
        resultSet = Mockito.mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @AfterEach
    void tearDown() throws SQLException {
        verify(connection).close();
    }

    @DisplayName("쿼리에 파라미터 정보를 올바르게 바인딩한다.")
    @Test
    void bindParameters() throws SQLException {
        // given
        String sql = "INSERT INTO users (account, password, email) VALUES (?, ?, ?)";
        String account = "user";
        String password = "password";
        String email = "user@example.org";
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);

        // when
        jdbcTemplate.update(sql, account, password, email);

        // then
        verify(preparedStatement).setObject(1, account);
        verify(preparedStatement).setObject(2, password);
        verify(preparedStatement).setObject(3, email);
        verify(preparedStatement).executeUpdate();
    }

    @DisplayName("쿼리 결과를 올바르게 매핑한다.")
    @Test
    void testQueryForObject() throws SQLException {
        // given
        String sql = "SELECT id, account, password, email FROM users WHERE account = ?";
        long id = 1L;
        String password = "password";

        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);

        when(resultSet.getLong("id")).thenReturn(id);
        when(resultSet.getString("password")).thenReturn(password);

        // when
        TestObject testObject = jdbcTemplate.queryForObject(sql, rs -> {
            try {
                return new TestObject(
                        rs.getLong("id"),
                        rs.getString("password")
                );
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, id, password);

        // then
        assertThat(id).isEqualTo(testObject.id());
        assertThat(password).isEqualTo(testObject.password());
    }

    @DisplayName("SQLException 발생 시 Unchecked Exception으로 래핑한다.")
    @Test
    void testQueryFail() throws SQLException {
        // given
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenThrow(new SQLException());

        // when & then
        assertThatThrownBy(() -> jdbcTemplate.update("BAD SQL"))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(SQLException.class);
    }

    private record TestObject(Long id, String password) {
    }
}
