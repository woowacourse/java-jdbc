package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.exception.EmptyResultException;
import org.springframework.jdbc.exception.NotSingleResultException;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class JdbcTemplateTest {

    private static final RowMapper<User> TEST_USER_MAPPER = rs -> {
        long id = rs.getLong("id");
        String account = rs.getString("account");
        String password = rs.getString("password");
        String email = rs.getString("email");
        return new User(id, account, password, email);
    };

    private PreparedStatement preparedStatement;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);

        given(dataSource.getConnection()).willReturn(connection);
        given(connection.prepareStatement(anyString())).willReturn(preparedStatement);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void execute_메서드를_호출한다() throws SQLException {
        String sql = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.execute(sql, "gray", "password", "gray@gmail.com");

        verify(preparedStatement, times(1)).execute();
    }

    @Test
    void query_메서드를_호출해_List_값을_반환한다() throws SQLException {
        String sql = "select * from users";
        ResultSet resultSet = mock(ResultSet.class);
        given(preparedStatement.executeQuery()).willReturn(resultSet);
        given(resultSet.next()).willReturn(true, false);

        List<User> results = jdbcTemplate.query(sql, TEST_USER_MAPPER);

        assertThat(results).isNotEmpty();
    }

    @Test
    void queryForObject_메서드를_호출해_단건_데이터을_반환한다() throws SQLException {
        String sql = "select * from users where id = ?";
        ResultSet resultSet = mock(ResultSet.class);
        given(preparedStatement.executeQuery()).willReturn(resultSet);
        given(resultSet.next()).willReturn(true, false);

        User user = jdbcTemplate.queryForObject(sql, TEST_USER_MAPPER, 1);

        assertThat(user).isNotNull();
    }

    @Test
    void queryForObject_메서드를_호출해_결과가_여러개_존재하면_예외_발생() throws SQLException {
        String sql = "select * from users where id = ?";
        ResultSet resultSet = mock(ResultSet.class);
        given(preparedStatement.executeQuery()).willReturn(resultSet);
        given(resultSet.next()).willReturn(true, true, false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, TEST_USER_MAPPER, 1))
                .isInstanceOf(NotSingleResultException.class)
                .hasMessage("결과가 2개 이상 존재합니다.");
    }

    @Test
    void queryForObject_메서드를_호출해_결과가_없으면_예외_발생() throws SQLException {
        String sql = "select * from users where id = ?";
        ResultSet resultSet = mock(ResultSet.class);
        given(preparedStatement.executeQuery()).willReturn(resultSet);
        given(resultSet.next()).willReturn(false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, TEST_USER_MAPPER, 1))
                .isInstanceOf(EmptyResultException.class)
                .hasMessage("일치하는 결과가 존재하지 않습니다.");
    }

    static class User {
        private Long id;
        private String account;
        private String password;
        private String email;

        public User(Long id, String account, String password, String email) {
            this.id = id;
            this.account = account;
            this.password = password;
            this.email = email;
        }
    }
}
