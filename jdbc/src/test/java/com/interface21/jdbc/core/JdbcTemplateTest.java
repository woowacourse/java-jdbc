package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.interface21.jdbc.exception.JdbcAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

class JdbcTemplateTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @DisplayName("UPDATE 쿼리를 실행한다.")
    @Test
    void update() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(1);

        int result = jdbcTemplate.update("UPDATE users SET name = ? WHERE id = ?", preparedStatement -> {
            preparedStatement.setString(1, "TRE");
            preparedStatement.setLong(2, 1L);
        });

        assertThat(result).isEqualTo(1);
        verify(preparedStatement).setString(1, "TRE");
        verify(preparedStatement).setLong(2, 1);
        verify(preparedStatement).executeUpdate();
    }

    @DisplayName("INSERT 쿼리를 실행한다.")
    @Test
    void insert() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(1);

        int result = jdbcTemplate.update("INSERT INTO users (name, email) VALUE (?, ?)", preparedStatement -> {
            preparedStatement.setString(1, "TRE");
            preparedStatement.setString(2, "TRE@gmail.com");
        });

        assertThat(result).isEqualTo(1);
        verify(preparedStatement).setString(1, "TRE");
        verify(preparedStatement).setString(2, "TRE@gmail.com");
        verify(preparedStatement).executeUpdate();
    }

    @DisplayName("조회 쿼리에 대한 목록을 반환한다.")
    @Test
    void queryForList() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("name")).thenReturn("Potato1", "Potato2");

        List<String> results = jdbcTemplate.queryForList("SELECT name FROM users", rs -> rs.getString("name"));

        assertThat(results).containsExactly("Potato1", "Potato2");
    }

    @DisplayName("파라미터를 설정하지 않는 조회 쿼리에 대한 목록을 반환한다.")
    @Test
    void queryFroListWithoutParameterSetter() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, true, false);
        when(resultSet.getLong("id")).thenReturn(1L, 2L, 3L);

        List<Long> result = jdbcTemplate.queryForList("SELECT id FROM users",
                rs -> rs.getLong("id"));

        assertThat(result).containsExactly(1L, 2L, 3L);
    }

    @DisplayName("조회 쿼리에 대한 객체를 반환한다.")
    @Test
    void queryForObject() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, true);
        when(resultSet.getString("name")).thenReturn("takoyakimchi");

        String result = jdbcTemplate.queryForObject("SELECT name FROM users WHERE id = ?",
                rs -> rs.getString("name"),
                parameterStatement -> parameterStatement.setLong(1, 1L)
        ).orElseThrow();

        assertThat(result).isEqualTo("takoyakimchi");
    }

    @DisplayName("파라미터를 설정하지 않는 조회 쿼리에 대한 객체를 반환한다.")
    @Test
    void queryFroObjectWithoutParameterSetter() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, true);
        when(resultSet.getInt("count")).thenReturn(1);

        int result = jdbcTemplate.queryForObject("SELECT COUNT(id) AS count FROM users",
                        rs -> rs.getInt("count"))
                .orElseThrow();

        assertThat(result).isEqualTo(1);
    }

    @DisplayName("쿼리를 실행한다.")
    @Test
    void execute() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(1);
        SqlFunction<PreparedStatement, Integer> action = PreparedStatement::executeUpdate;

        int result = jdbcTemplate.execute("DELETE FROM users WHERE id = 1", action);

        assertThat(result).isEqualTo(1);
        verify(preparedStatement).executeUpdate();
    }

    @DisplayName("SQLException이 발생하는 경우 RuntimeException으로 변환해 준다.")
    @Test
    void convertSQLException() {
        SqlFunction<PreparedStatement, Integer> action = (preparedStatement) -> {
            throw new SQLException("잘못된 요청입니다.");
        };

        assertThatThrownBy(() -> jdbcTemplate.execute("INSERT into", action))
                .isInstanceOf(JdbcAccessException.class)
                .hasMessageContaining("INSERT into");
    }
}
