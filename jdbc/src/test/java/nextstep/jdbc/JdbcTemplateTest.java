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
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;
    private Connection connection;
    private PreparedStatement statement;

    @BeforeEach
    void setup() throws SQLException {
        final DataSource dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        jdbcTemplate = new JdbcTemplate(dataSource);

        given(dataSource.getConnection()).willReturn(connection);
        given(connection.prepareStatement(any())).willReturn(statement);
    }

    @Test
    @DisplayName("query 메서드는 SQL 문으로 조회된 객체 목록을 반환한다.")
    void query() throws SQLException {
        // given
        final ResultSet resultSet = mock(ResultSet.class);
        final String sql = "select id, account from users";
        final RowMapper<String> rowMapper = (rs, rowNum) -> rs.getString("account");

        given(statement.executeQuery()).willReturn(resultSet);
        given(resultSet.next())
                .willReturn(true)
                .willReturn(true)
                .willReturn(false);
        given(resultSet.getString("account"))
                .willReturn("pepper")
                .willReturn("gugu");

        // when
        final List<String> actual = jdbcTemplate.query(sql, rowMapper);

        // then
        assertAll(
                () -> assertThat(actual).contains("pepper", "gugu"),
                () -> verify(statement).executeQuery(),
                () -> verify(connection).close(),
                () -> verify(statement).close()
        );
    }

    @Test
    @DisplayName("queryForObject 메서드는 SQL 문으로 조회된 단일 객체를 반환한다.")
    void queryForObject() throws SQLException {
        // given
        final ResultSet resultSet = mock(ResultSet.class);
        final String sql = "select id, account, password, email from users where account = ?";
        final RowMapper<String> rowMapper = (rs, rowNum) -> rs.getString("account");

        given(statement.executeQuery()).willReturn(resultSet);
        given(resultSet.next())
                .willReturn(true)
                .willReturn(false);
        given(resultSet.getString("account"))
                .willReturn("pepper");

        // when
        final String actual = jdbcTemplate.queryForObject(sql, rowMapper, "pepper");

        // then
        assertAll(
                () -> assertThat(actual).isEqualTo("pepper"),
                () -> verify(statement).setObject(1, "pepper"),
                () -> verify(statement).executeQuery(),
                () -> verify(connection).close(),
                () -> verify(statement).close()
        );
    }

    @Test
    @DisplayName("update 메서드는 SQL 문을 실행한다.")
    void update() {
        // given
        final String sql = "update users set account = ?, password = ?, email = ? where id = ?";

        // when
        jdbcTemplate.update(sql, "pepper", "123!", "pepper@wooteco.com", 1);

        // then
        assertAll(
                () -> verify(statement).setObject(1, "pepper"),
                () -> verify(statement).executeUpdate(),
                () -> verify(connection).close(),
                () -> verify(statement).close()
        );
    }
}
