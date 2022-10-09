package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sql.DataSource;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class JdbcTemplateTest {

    private Connection connection;
    private DataSource dataSource;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        this.connection = mock(Connection.class);
        this.dataSource = mock(DataSource.class);
        this.preparedStatement = mock(PreparedStatement.class);
        this.resultSet = mock(ResultSet.class);
        this.jdbcTemplate = new JdbcTemplate(this.dataSource);

        given(this.dataSource.getConnection()).willReturn(this.connection);
        given(this.connection.prepareStatement(anyString())).willReturn(this.preparedStatement);
        given(this.preparedStatement.executeQuery()).willReturn(this.resultSet);
        given(this.preparedStatement.executeQuery(anyString())).willReturn(this.resultSet);
        given(this.preparedStatement.getConnection()).willReturn(this.connection);
    }

    @DisplayName("select 실행 시 적절한 값을 가져오고 자원을 반납한다")
    @Test
    void query() throws SQLException {
        // given
        mockResultSet(1);

        // when
        final String actual =
                jdbcTemplate.queryOne("select from users where id = ?",
                        rs -> rs.getString("account"),
                        1L);

        // then
        assertAll(
                () -> assertThat(actual).isEqualTo("philz"),
                verifyConnectionClose(),
                verifyStatementClose()
        );
    }

    @DisplayName("insert 실행 시, 매개 변수를 세팅하고 자원을 반납한다")
    @Test
    void insert() {
        // given
        final User user = new User(1L, "philz", "1234", "philz@wooteco.com");

        // when
        jdbcTemplate.execute(
                "insert into users (account, password, email) values (?, ?, ?) ",
                user.getAccount(), user.getPassword(), user.getEmail()
        );

        // then
        assertAll(
                () -> verifyStatement().setObject(1, "philz"),
                () -> verifyStatement().setObject(2, "1234"),
                () -> verifyStatement().setObject(3, "philz@wooteco.com"),
                () -> verifyStatementClose(),
                () -> verifyConnectionClose()
        );
    }

    @DisplayName("select for List 시 List로 응답한다")
    @Test
    void query_list() throws SQLException {
        // given
        mockResultSet(3);

        // when
        final List<String> actual = jdbcTemplate.queryAll(
                "select from users",
                rs -> rs.getString("account")
        );

        // then
        assertAll(
                () -> assertThat(actual).hasSize(3),
                () -> assertThat(actual).containsExactlyInAnyOrder("philz", "philz", "philz"),
                verifyStatementClose(),
                verifyConnectionClose()
        );
    }

    @DisplayName("queryOne 메서드는 결과가 없으면 null을 반환한다.")
    @Test
    void queryOne_exception_when_no_data() throws SQLException {
        // given
        mockResultSetNull();

        // when
        final String actual =
                jdbcTemplate.queryOne(
                        "select from users where id = ?",
                        rs -> rs.getString("account"),
                        101L);

        // then
        assertAll(
                () -> assertThat(actual).isNull(),
                verifyStatementClose(),
                verifyConnectionClose()
        );
    }

    @DisplayName("queryForOne 메서드는 결과 행이 2 이상이면 예외를 던진다")
    @Test
    void queryOne_exception_when_data_size_2() throws SQLException {

        // given
        final AtomicInteger dataRowsObject = new AtomicInteger(2);
        given(resultSet.next()).will(invocation -> dataRowsObject.getAndDecrement() > 0);
        given(resultSet.getString("account")).willReturn("philz");

        // when & then
        final ThrowingCallable callable = () -> jdbcTemplate.queryOne(
                "select from users where name = ?",
                rs -> rs.getString("account"),
                101L);

        assertAll(
                () -> assertThatThrownBy(callable)
                        .isInstanceOf(DataAccessException.class)
                        .hasMessageContaining("Expected Result Size One, But Size 2"),
                verifyStatementClose(),
                verifyConnectionClose()
        );
    }

    private void mockResultSet(final int dataRows) throws SQLException {
        final AtomicInteger dataRowsObject = new AtomicInteger(dataRows);
        given(resultSet.next()).will(invocation -> dataRowsObject.getAndDecrement() > 0);
        given(resultSet.getString("account")).willReturn("philz");
    }

    private void mockResultSetNull() throws SQLException {
        final AtomicInteger dataRowsObject = new AtomicInteger(0);
        given(resultSet.next()).will(invocation -> dataRowsObject.getAndDecrement() > 0);
        given(resultSet.next()).willReturn(false);
        given(resultSet.getString("account")).willReturn(null);
    }

    private PreparedStatement verifyStatement() {
        return verify(preparedStatement, times(1));
    }

    private Executable verifyStatementClose() {
        return () -> verifyStatement().close();
    }

    private Executable verifyConnectionClose() {
        return () -> verify(connection, times(1)).close();
    }

    private static class User {

        private final Long id;
        private final String account;
        private final String password;
        private final String email;

        public User(final Long id, final String account, final String password, final String email) {
            this.id = id;
            this.account = account;
            this.password = password;
            this.email = email;
        }

        public User(final String account, final String password, final String email) {
            this(null, account, password, email);
        }

        public Long getId() {
            return id;
        }

        public String getAccount() {
            return account;
        }

        public String getPassword() {
            return password;
        }

        public String getEmail() {
            return email;
        }
    }
}