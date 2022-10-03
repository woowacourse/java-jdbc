package nextstep.jdbc;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private Connection connection;
    private PreparedStatement statement;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        jdbcTemplate = new JdbcTemplate(dataSource);

        given(dataSource.getConnection())
                .willReturn(connection);
        given(connection.prepareStatement(anyString()))
                .willReturn(statement);
    }

    @Test
    @DisplayName("쿼리와 파라미터를 받아 실행시켜 내부 데이터를 업데이트 한다.")
    void updateWithParams() throws SQLException {
        // given
        willDoNothing().given(statement).setString(anyInt(), anyString());
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";

        // when
        jdbcTemplate.update(sql, "account", "password", "email");

        // then
        assertAll(
                () -> verify(statement, times(3))
                        .setString(anyInt(), anyString()),
                () -> verify(statement).executeUpdate(),
                () -> verify(statement).close(),
                () -> verify(connection).close()
        );
    }

    @Test
    @DisplayName("쿼리와 파라미터를 받아 실행시켜 내부 데이터를 업데이트 한다.")
    void updateWithoutParams() {
        // given
        final String sql = "delete from users";

        // when
        jdbcTemplate.update(sql);

        // then
        assertAll(
                () -> verify(statement).executeUpdate(),
                () -> verify(statement).close(),
                () -> verify(connection).close()
        );
    }

    @Test
    @DisplayName("쿼리를 실행시켜 내부 데이터를 조회한다.")
    void find() throws SQLException {
        // given
        willDoNothing().given(statement).setLong(1, 1L);
        final String sql = "select id, account, password, email from users where id = ?";

        // when
        jdbcTemplate.find(sql, 1L);
        
        // then
        assertAll(
                () -> verify(statement).setLong(1, 1L),
                () -> verify(statement).executeQuery(),
                () -> verify(statement).close(),
                () -> verify(connection).close()
        );
    }
}
