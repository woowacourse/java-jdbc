package nextstep.jdbc;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;
    private Connection connection;
    private PreparedStatement statement;

    @BeforeEach
    void setUp() throws SQLException {
        final DataSource dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        jdbcTemplate = new JdbcTemplate(dataSource);

        given(dataSource.getConnection()).willReturn(connection);
        given(connection.prepareStatement(any())).willReturn(statement);
    }

    @Test
    @DisplayName("execute 메서드는 주어진 sql문과 param Map을 이용해 쿼리를 실행한다.")
    void execute() {
        // given & when
        jdbcTemplate.execute("insert into users (account, password, email) values (?, ?, ?)",
                Map.of(1, "account", 2, "password", 3, "email"));

        // then
        assertAll(
                () -> verify(statement).setObject(1, "account"),
                () -> verify(statement).execute(),
                () -> verify(connection).close(),
                () -> verify(statement).close()
        );
    }
}
