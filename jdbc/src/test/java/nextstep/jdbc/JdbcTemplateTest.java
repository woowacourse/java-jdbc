package nextstep.jdbc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final PreparedStatement preparedStatement = mock(PreparedStatement.class);
    private final ResultSet resultSet = mock(ResultSet.class);

    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

    @BeforeEach
    void setUp() throws SQLException {
        when(dataSource.getConnection())
                .thenReturn(connection);
        when(connection.prepareStatement(anyString()))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeQuery())
                .thenReturn(resultSet);
    }

    @Test
    @DisplayName("executeUpdate를 할 때 파라미터들을 PreparedStatement에 세팅한다")
    void executeUpdate() throws SQLException {
        //when
        jdbcTemplate.executeUpdate("INSERT INTO users (account, name) VALUES (?, ?)", "연어", "재현");

        //then
        verify(preparedStatement, times(1)).setObject(1, "연어");
        verify(preparedStatement, times(1)).setObject(2, "재현");
        verify(preparedStatement, times(1)).executeUpdate();
    }

    @Test
    @DisplayName("executeUpdate를 할 때 파라미터가 없다면 executeUpdate만 호출한다")
    void executeUpdate_noParameters() throws SQLException {
        //when
        jdbcTemplate.executeUpdate("DELETE FROM users");

        //then
        verify(preparedStatement, never()).setObject(anyInt(), any());
        verify(preparedStatement, times(1)).executeUpdate();
    }

    @Test
    @DisplayName("queryForObject로 한 튜플을 가져올 수 있다")
    void queryForObject() throws SQLException {
        //given
        when(resultSet.next()).thenReturn(true, false);

        //when
        final Object result = jdbcTemplate.queryForObject("SELECT * FROM users", rs -> new Object());

        //then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("queryForObject의 결과 튜플이 1개가 아닐 때는 예외가 발생한다")
    void queryForObject_fail() throws SQLException {
        //given
        when(resultSet.next()).thenReturn(true, true, false);

        //when
        assertThatThrownBy(() -> jdbcTemplate.queryForObject("SELECT * FROM users", rs -> new Object()))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    @DisplayName("queryForList로 결과 튜플들 전체를 리스트로 응답한다")
    void queryForList() throws SQLException {
        //given
        when(resultSet.next()).thenReturn(true, true, true, false);

        //when
        final List<Object> objects = jdbcTemplate.queryForList("SELECT * FROM users", rs -> new Object());

        //then
        assertThat(objects).hasSize(3);
    }
}
