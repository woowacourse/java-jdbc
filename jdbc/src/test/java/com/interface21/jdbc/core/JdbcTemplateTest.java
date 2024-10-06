package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class JdbcTemplateTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @InjectMocks // @Mock을 붙인 객체를 이 어노테이션이 붙은 객체를 생성할때 주입한다. 여기선 DataSource가 주입된다.
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        when(dataSource.getConnection()).thenReturn(connection); // JdbcTemplate에 주입된 DataSource가 모킹 커넥션 반환
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement); // 모킹 커넥션이 모킹 스테이트먼트 반환
    }

    @Test
    @DisplayName("execute 동작 확인")
    public void testExecute() throws SQLException {
        String sql = "UPDATE users SET name = ? WHERE id = ?";
        Object[] params = {"John", 1};

        when(preparedStatement.executeUpdate()).thenReturn(1);

        int rowsAffected = jdbcTemplate.execute(sql, params);

        assertThat(rowsAffected).isEqualTo(1);

        verify(preparedStatement).setString(1, "John");
        verify(preparedStatement).setString(2, "1");
        verify(preparedStatement).executeUpdate();
    }

    @Test
    @DisplayName("query 동작 확인")
    public void testQuery() throws SQLException {
        String sql = "SELECT id, name FROM users WHERE id = ?";
        Object[] params = {1};

        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("name")).thenReturn("John");

        ResultSetParser<User> parser = userResultSetParser();

        List<User> users = jdbcTemplate.query(sql, parser, params);

        assertThat(users).isNotNull().hasSize(1);
        assertThat(users.getFirst().getName()).isEqualTo("John");

        verify(preparedStatement).setString(1, "1");
        verify(preparedStatement).executeQuery();
    }

    @Test
    @DisplayName("queryOne 동작 확인")
    public void testQueryOne() throws SQLException {
        String sql = "SELECT id, name FROM users WHERE id = ?";
        Object[] params = {1};

        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("name")).thenReturn("John");

        ResultSetParser<User> parser = userResultSetParser();

        User user = jdbcTemplate.queryOne(sql, parser, params);

        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo("John");

        verify(preparedStatement).setString(1, "1");
        verify(preparedStatement).executeQuery();
    }

    private CastingResultSetParser<User> userResultSetParser() {
        return new CastingResultSetParser<>(User.class) {
            @Override
            protected Object parseInternal(ResultSet resultSet) throws SQLException {
                return new User(resultSet.getInt("id"), resultSet.getString("name"));
            }
        };
    }

    @Test
    @DisplayName("queryOne 조회 데이터가 많을 때 예외 던지는지 확인")
    public void testQueryOne_TooManyRows() throws SQLException {
        String sql = "SELECT id, name FROM users WHERE id = ?";
        Object[] params = {1};

        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);  // Multiple rows

        ResultSetParser<User> parser = userResultSetParser();

        assertThatThrownBy(() -> jdbcTemplate.queryOne(sql, parser, params))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("여러개의 행이 조회되었습니다.");

        verify(preparedStatement).setString(1, "1");
        verify(preparedStatement).executeQuery();
    }

    @Test
    @DisplayName("queryOne 조회 데이터가 없을 때 예외 던지는지 확인")
    public void testQueryOne_NoRows() throws SQLException {
        String sql = "SELECT id, name FROM users WHERE id = ?";
        Object[] params = {1};

        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);  // No rows

        ResultSetParser<User> parser = userResultSetParser();

        assertThatThrownBy(() -> jdbcTemplate.queryOne(sql, parser, params))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("행이 하나도 조회되지 않았습니다.");

        verify(preparedStatement).setString(1, "1");
        verify(preparedStatement).executeQuery();
    }
}
