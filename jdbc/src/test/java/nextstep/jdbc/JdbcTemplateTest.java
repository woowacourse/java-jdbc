package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private final RowMapper<User> userRowMapper = rs -> new User(
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email")
    );

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    private final Connection connection;
    private final PreparedStatement statement;
    private final ResultSet resultSet;

    public JdbcTemplateTest() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        jdbcTemplate = new JdbcTemplate(dataSource);

        given(dataSource.getConnection()).willReturn(connection);
        given(connection.prepareStatement(any())).willReturn(statement);
        given(statement.executeQuery()).willReturn(resultSet);
    }

    @DisplayName("update는 sql 쿼리를 실행시킨다")
    @Test
    void update() {
        // given
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        final String account = "alien";
        final String password = "password";
        final String email = "alien@mail.com";

        // when
        jdbcTemplate.update(sql, account, password, email);

        // then
        assertAll(
                () -> verify(statement).setObject(1, account),
                () -> verify(statement).setObject(2, password),
                () -> verify(statement).setObject(3, email),
                () -> verify(statement).executeUpdate(),
                () -> verify(statement).close(),
                () -> verify(connection).close()
        );
    }

    @DisplayName("queryForObject는 한개의 결과를 반환한다.")
    @Test
    void queryForObject() throws Exception {
        // given
        final String account = "alien";
        final var sql = "select id, account, password, email from users where account = ?";

        given(resultSet.next()).willReturn(true, false);

        // when
        final User user = jdbcTemplate.queryForObject(sql, userRowMapper, account);

        // then
        assertAll(
                () -> assertThat(user).isNotNull(),
                () -> verify(statement).setObject(1, account),
                () -> verify(resultSet).getLong("id"),
                () -> verify(resultSet).getString("account"),
                () -> verify(resultSet).getString("password"),
                () -> verify(resultSet).getString("email"),
                () -> verify(statement).executeQuery(),
                () -> verify(statement).close(),
                () -> verify(connection).close()
        );
    }

    @DisplayName("queryForObject는 결과가 없으면 예외를 던진다.")
    @Test
    void queryForObjectWithEmpty() {
        // when & then
        final var sql = "select id, account, password, email from users where account = ?";

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, userRowMapper, "alien"))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("데이터가 없습니다.");
    }

    @DisplayName("queryForObject는 결과가 두개 이상이면 예외가 던진다.")
    @Test
    void queryForObjectWithMultiData() throws Exception {
        // given
        given(resultSet.next()).willReturn(true, true, false);

        final var sql = "select id, account, password, email from users";

        // when & then
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, userRowMapper))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("데이터가 한개가 아닙니다.");
    }

    @DisplayName("query는 결과를 List형으로 반환한다.")
    @Test
    void query() throws Exception {
        // given
        given(resultSet.next()).willReturn(true, true, false);

        final var sql = "select id, account, password, email from users";

        // when
        final List<User> users = jdbcTemplate.query(sql, userRowMapper);

        // then
        assertThat(users).hasSize(2);
    }

    static class User {

        private Long id;
        private final String account;
        private String password;
        private final String email;

        public User(final long id, final String account, final String password, final String email) {
            this.id = id;
            this.account = account;
            this.password = password;
            this.email = email;
        }

        public User(final String account, final String password, final String email) {
            this.account = account;
            this.password = password;
            this.email = email;
        }

        public boolean checkPassword(final String password) {
            return this.password.equals(password);
        }

        public void changePassword(final String password) {
            this.password = password;
        }

        public String getAccount() {
            return account;
        }

        public long getId() {
            return id;
        }

        public String getEmail() {
            return email;
        }

        public String getPassword() {
            return password;
        }
    }
}
