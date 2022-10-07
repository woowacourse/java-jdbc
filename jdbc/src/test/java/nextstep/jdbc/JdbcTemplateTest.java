package nextstep.jdbc;

import static java.sql.ResultSet.CONCUR_READ_ONLY;
import static java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.support.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        this.dataSource = mock(DataSource.class);
        this.connection = mock(Connection.class);
        this.preparedStatement = mock(PreparedStatement.class);
        this.resultSet = mock(ResultSet.class);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Nested
    @DisplayName("update 메서드는")
    class Update {

        @Test
        @DisplayName("CUD 쿼리를 실행한다.")
        void success() throws SQLException {
            // given
            final String sql = "INSERT INTO users(account, password, email) VALUES(?, ?, ?)";
            final String account = "rick";
            final String password = "1q2w3e4r";
            final String email = "rick@levellog.app";

            given(dataSource.getConnection()).willReturn(connection);
            given(connection.prepareStatement(sql, TYPE_SCROLL_INSENSITIVE, CONCUR_READ_ONLY)).willReturn(
                    preparedStatement);
            given(preparedStatement.executeUpdate()).willReturn(1);

            // when
            jdbcTemplate.update(sql, account, password, email);

            // then
            verify(preparedStatement, times(3)).setObject(anyInt(), anyString());
        }
    }

    @Nested
    @DisplayName("queryForObject 메서드는")
    class QueryForObject {

        @Test
        @DisplayName("일치하는 레코드를 조회 후 RowMapper로 객체를 생성해서 반환한다.")
        void success() throws SQLException {
            // given
            final String sql = "SELECT id, account, password, email FROM users WHERE account = ?";
            final String account = "rick";

            given(dataSource.getConnection()).willReturn(connection);
            given(connection.prepareStatement(sql, TYPE_SCROLL_INSENSITIVE, CONCUR_READ_ONLY)).willReturn(
                    preparedStatement);
            given(preparedStatement.executeQuery()).willReturn(resultSet);
            given(resultSet.getRow()).willReturn(1);
            given(resultSet.getString("account")).willReturn(account);

            // when
            final User actual = jdbcTemplate.queryForObject(sql, User.ROW_MAPPER, account);

            // then
            assertThat(actual).extracting(User::getAccount)
                    .isEqualTo(account);
            verify(resultSet, times(1)).last();
            verify(resultSet, times(1)).first();
            verify(resultSet, times(1)).getLong(anyString());
            verify(resultSet, times(3)).getString(anyString());
        }

        @Test
        @DisplayName("일치하는 레코드가 한 건 이상이면 예외를 던진다.")
        void queryForObject_moreThenOne_exception() throws SQLException {
            // given
            final String sql = "SELECT id, account, password, email FROM users WHERE account = ?";
            final String account = "rick";

            given(dataSource.getConnection()).willReturn(connection);
            given(connection.prepareStatement(sql, TYPE_SCROLL_INSENSITIVE, CONCUR_READ_ONLY)).willReturn(
                    preparedStatement);
            given(preparedStatement.executeQuery()).willReturn(resultSet);
            given(resultSet.getRow()).willReturn(2);

            // when & then
            assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, User.ROW_MAPPER, account))
                    .isInstanceOf(IncorrectResultSizeDataAccessException.class)
                    .hasMessage("Incorrect result size: expected 1 but 2");

            verify(resultSet, times(1)).last();
            verify(resultSet, never()).first();
            verify(resultSet, never()).getLong(anyString());
            verify(resultSet, never()).getString(anyString());
        }

        @Test
        @DisplayName("일치하는 레코드가 존재하지 않으면 예외를 던진다.")
        void queryForObject_recordNotExist_exception() throws SQLException {
            // given
            final String sql = "SELECT id, account, password, email FROM users WHERE account = ?";
            final String account = "rick";

            given(dataSource.getConnection()).willReturn(connection);
            given(connection.prepareStatement(sql, TYPE_SCROLL_INSENSITIVE, CONCUR_READ_ONLY)).willReturn(
                    preparedStatement);
            given(preparedStatement.executeQuery()).willReturn(resultSet);
            given(resultSet.getRow()).willReturn(0);

            // when & then
            assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, User.ROW_MAPPER, account))
                    .isInstanceOf(EmptyResultDataAccessException.class)
                    .hasMessage("Incorrect result size: expected 1 but 0");

            verify(resultSet, times(1)).last();
            verify(resultSet, never()).first();
            verify(resultSet, never()).getLong(anyString());
            verify(resultSet, never()).getString(anyString());
        }
    }

    @Nested
    @DisplayName("query 메서드는")
    class Query {

        @Test
        @DisplayName("일치하는 레코드를 List로 반환한다.")
        void success() throws SQLException {
            // given
            final String sql = "SELECT id, account, password, email FROM users";
            final String rickAccount = "rick";
            final String romaAccount = "roma";

            given(dataSource.getConnection()).willReturn(connection);
            given(connection.prepareStatement(sql, TYPE_SCROLL_INSENSITIVE, CONCUR_READ_ONLY)).willReturn(
                    preparedStatement);
            given(preparedStatement.executeQuery()).willReturn(resultSet);
            given(resultSet.getString("account")).willReturn(rickAccount, romaAccount);
            given(resultSet.next()).willReturn(true, true, false);

            // when
            final List<User> actual = jdbcTemplate.query(sql, User.ROW_MAPPER);

            // then
            assertThat(actual).hasSize(2)
                    .extracting(User::getAccount)
                    .containsExactly(rickAccount, romaAccount);
            verify(resultSet, times(2)).getLong(anyString());
            verify(resultSet, times(6)).getString(anyString());
            verify(resultSet, times(3)).next();
        }

        @Test
        @DisplayName("일치하는 레코드가 존재하지 않으면 빈 리스트를 반환한다.")
        void success_recordNotExist_emptyList() throws SQLException {
            // given
            final String sql = "SELECT id, account, password, email FROM users";

            given(dataSource.getConnection()).willReturn(connection);
            given(connection.prepareStatement(sql, TYPE_SCROLL_INSENSITIVE, CONCUR_READ_ONLY)).willReturn(
                    preparedStatement);
            given(preparedStatement.executeQuery()).willReturn(resultSet);
            given(resultSet.next()).willReturn(false);

            // when
            final List<User> actual = jdbcTemplate.query(sql, User.ROW_MAPPER);

            // then
            assertThat(actual).isEmpty();
            verify(resultSet, never()).getLong(anyString());
            verify(resultSet, never()).getString(anyString());
            verify(resultSet, times(1)).next();
        }
    }
}
