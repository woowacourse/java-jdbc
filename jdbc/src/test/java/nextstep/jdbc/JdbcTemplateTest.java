package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
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
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.execute(sql,
                Map.of(1, "account", 2, "password", 3, "email"));

        // then
        assertAll(
                () -> verify(statement).setObject(1, "account"),
                () -> verify(statement).execute(),
                () -> verify(connection).close(),
                () -> verify(statement).close()
        );
    }

    @Test
    @DisplayName("query 메서드는 주어진 sql문과 param Map을 이용해 쿼리를 실행하고 주어진 function에 따라 객체를 반환한다.")
    void query() throws SQLException {
        // given
        final ResultSet resultSet = mock(ResultSet.class);

        given(statement.executeQuery()).willReturn(resultSet);
        given(resultSet.next()).willReturn(true);
        given(resultSet.getString("account")).willReturn("roma");
        given(resultSet.getString("password")).willReturn("1234");
        given(resultSet.getString("email")).willReturn("roma@service.apply");

        // when
        final String sql = "select account, password, email from users where account = ?";
        final String result = jdbcTemplate.query(sql, Map.of(1, "roma"),
                rs -> {
                    try {
                        return String.format("%s/%s/%s",
                                rs.getString("account"),
                                rs.getString("password"),
                                rs.getString("email"));
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
        );

        // then
        assertAll(
                () -> assertThat(result).isEqualTo("roma/1234/roma@service.apply"),
                () -> verify(statement).setObject(1, "roma"),
                () -> verify(statement).executeQuery(),
                () -> verify(connection).close(),
                () -> verify(statement).close(),
                () -> verify(resultSet).close()
        );
    }

    @Test
    @DisplayName("queryForList 메서드는 주어진 sql문과 param Map을 이용해 쿼리를 실행하고 주어진 function에 따른 객체의 리스트를 반환한다.")
    void queryForList() throws SQLException {
        // given
        final ResultSet resultSet = mock(ResultSet.class);

        given(statement.executeQuery()).willReturn(resultSet);
        given(resultSet.next())
                .willReturn(true)
                .willReturn(true)
                .willReturn(false);
        given(resultSet.getString("account"))
                .willReturn("roma")
                .willReturn("jason");
        given(resultSet.getString("password"))
                .willReturn("1234")
                .willReturn("4321");
        given(resultSet.getString("email"))
                .willReturn("roma@service.apply")
                .willReturn("jason@service.apply");

        // when
        final String sql = "select account, password, email from users";
        final List<String> result = jdbcTemplate.queryForList(sql, Map.of(),
                rs -> {
                    try {
                        return String.format("%s/%s/%s",
                                rs.getString("account"),
                                rs.getString("password"),
                                rs.getString("email"));
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
        );

        // then
        assertAll(
                () -> assertThat(result).contains("roma/1234/roma@service.apply", "jason/4321/jason@service.apply"),
                () -> verify(statement).executeQuery(),
                () -> verify(connection).close(),
                () -> verify(statement).close(),
                () -> verify(resultSet).close()
        );

    }
}
