package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import nextstep.example.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

class JdbcTemplateTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<User> rowMapper = resultSet -> new User(
            resultSet.getLong("id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email")
    );

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);
        jdbcTemplate = new JdbcTemplate(dataSource);

        given(dataSource.getConnection()).willReturn(connection);
        given(connection.prepareStatement(anyString())).willReturn(statement);
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
                        .setObject(anyInt(), anyString()),
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
        given(statement.executeQuery()).willReturn(resultSet);
        willDoNothing().given(statement).setLong(1, 1L);
        given(resultSet.getLong(anyString())).willReturn(1L);
        given(resultSet.getString(anyString())).willReturn("returnAny");
        given(resultSet.next()).willReturn(true, false);

        final String sql = "select id, account, password, email from users where id = ?";

        // when
        jdbcTemplate.find(sql, rowMapper, 1L);
        
        // then
        assertAll(
                () -> verify(statement).setObject(1, 1L),
                () -> verify(statement).executeQuery(),
                () -> verify(resultSet).getLong(anyString()),
                () -> verify(resultSet, times(3)).getString(anyString()),
                () -> verify(statement).close(),
                () -> verify(connection).close()
        );
    }

    @Test
    @DisplayName("find 메서드 결과 값이 2개 이상인 경우 조회 성공한다.")
    void find_multiple_results() throws SQLException {
        // given
        final String sql = "select id, account, password, email from users";

        given(statement.executeQuery()).willReturn(resultSet);
        given(resultSet.next()).willReturn(true, true, false);
        given(resultSet.getLong("id")).willReturn(1L, 2L);
        given(resultSet.getString("account")).willReturn("a", "b");
        given(resultSet.getString("password")).willReturn("aa", "bb");
        given(resultSet.getString("email")).willReturn("aaa@naver.com", "bbb@naver.com");

        // when
        final List<User> actual = jdbcTemplate.find(sql, rowMapper, 1);

        // then
        assertAll(
                () -> assertThat(actual).hasSize(2),
                () -> verify(statement).executeQuery(),
                () -> verify(resultSet, times(3)).next(),
                () -> verify(resultSet, times(2)).getLong("id"),
                () -> verify(resultSet, times(2)).getString("account"),
                () -> verify(resultSet, times(2)).getString("password"),
                () -> verify(resultSet, times(2)).getString("email"),
                () -> verify(connection).close()
        );
    }

    @Test
    @DisplayName("findSingleResult를 했을 떄 반환 값이 하나가 아닐 때 예외가 발생한다.")
    void findSingleResult_multipleResult_Exception() throws SQLException {
        // given
        final String sql = "select id, account, email, password from users";

        given(statement.executeQuery()).willReturn(resultSet);
        given(resultSet.next()).willReturn(true, true, false);
        given(resultSet.getLong("id")).willReturn(1L, 2L);
        given(resultSet.getString("account")).willReturn("a", "b");
        given(resultSet.getString("password")).willReturn("aa", "bb");
        given(resultSet.getString("email")).willReturn("aaa@naver.com", "bbb@naver.com");

        // when, then
        assertThatThrownBy(() -> jdbcTemplate.findSingleResult(sql, rowMapper))
                .isExactlyInstanceOf(DataAccessException.class);
    }
    
    @Test
    @DisplayName("트랜잭션 상황에서 메서드가 실행될 경우 connection을 닫지 않는다.")
    void notCloseConnectionTransaction() {
        // given
        final String sql = "delete from users";
        final DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        final TransactionStatus transaction = transactionManager.getTransaction(new DefaultTransactionDefinition());

        // when
        jdbcTemplate.update(sql);

        // then
        assertAll(
                () -> verify(statement).executeUpdate(),
                () -> verify(statement).close(),
                () -> verify(connection, times(0)).close()
        );
        transactionManager.commit(transaction);
    }
}
