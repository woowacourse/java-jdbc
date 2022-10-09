package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private static final String SQL = "";
    private static final RowMapper<String> ROW_MAPPER = rs -> "";

    private JdbcTemplate jdbcTemplate;
    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        preparedStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        resultSet = mock(ResultSet.class);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @DisplayName("query 메서드 호출 시 모든 자원이 해제됨을 확인한다.")
    @Test
    void query() throws SQLException {
        jdbcTemplate.query(SQL, ROW_MAPPER);

        verify(resultSet).close();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @DisplayName("queryForObject 메서드 호출 시 모든 자원이 해제됨을 확인한다.")
    @Test
    void queryForObject() throws SQLException {
        when(resultSet.next())
                .thenReturn(true)
                .thenReturn(false);
        jdbcTemplate.queryForObject(SQL, ROW_MAPPER);

        verify(resultSet).close();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @DisplayName("execute 메서드 호출 시 모든 자원이 해제됨을 확인한다.")
    @Test
    void execute() throws SQLException {
        jdbcTemplate.execute(SQL);

        verify(preparedStatement).close();
        verify(connection).close();
    }

    @DisplayName("queryForObject의 실행 결과가 없으면 예외를 발생한다.")
    @Test
    void throwsExceptionWhenResultDoesNotExisted() throws SQLException {
        when(resultSet.next())
                .thenReturn(false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(SQL, ROW_MAPPER))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("1이 아닙니다.");
    }

    @DisplayName("queryForObject의 실행 결과 2 이상이면 예외를 발생한다.")
    @Test
    void throwsExceptionWhenResultSizeIs2() throws SQLException {
        when(resultSet.next())
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(SQL, ROW_MAPPER))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("1이 아닙니다.");
    }
}
