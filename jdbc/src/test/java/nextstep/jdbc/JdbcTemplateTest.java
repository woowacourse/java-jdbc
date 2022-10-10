package nextstep.jdbc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;

    @BeforeEach
    public void setup() throws Exception {
        this.connection = mock(Connection.class);
        this.dataSource = mock(DataSource.class);
        this.statement = mock(PreparedStatement.class);
        this.resultSet = mock(ResultSet.class);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        given(dataSource.getConnection()).willReturn(connection);

        given(connection.prepareStatement(anyString())).willReturn(statement);

        given(statement.executeQuery()).willReturn(resultSet);
        given(statement.getConnection()).willReturn(connection);
    }

    @Test
    @DisplayName("update시 호출하는 메서드를 검증한다.")
    void update() {
        // given
        final String sql = "update users set account = ? where id = ?";

        // when
        jdbcTemplate.update(sql, "update", 1L);

        // then
        assertAll(
            () -> verify(statement).executeUpdate(),
            () -> verify(statement).setObject(1, "update"),
            () -> verify(statement).close(),
            () -> verify(connection).close()
        );
    }

    @Test
    @DisplayName("select시 호출하는 메서드를 검증한다.")
    void select() throws SQLException {
        // given
        final String sql = "select id, account, password, email from users where id = ?";
        final ObjectMapper<TestUser> objectMapper = (ResultSet rs, int rowNum) ->
            new TestUser(
                rs.getLong("id"),
                rs.getString("account"),
                rs.getString("password"),
                rs.getString("email")
            );
        given(resultSet.next()).willReturn(true, false);

        // when
        jdbcTemplate.select(sql, objectMapper, 1L);

        // then
        assertAll(
            () -> verify(statement).executeQuery(),
            () -> verify(statement).setObject(1, 1L),

            () -> verify(resultSet).getLong("id"),
            () -> verify(resultSet).getString("account"),
            () -> verify(resultSet).getString("password"),
            () -> verify(resultSet).getString("email"),

            () -> verify(statement).close(),
            () -> verify(connection).close()
        );
    }
}
