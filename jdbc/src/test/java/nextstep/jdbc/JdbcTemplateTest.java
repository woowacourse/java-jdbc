package nextstep.jdbc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;
    private DataSource dataSourceMock;
    private Connection connectionMock;
    private PreparedStatement preparedStatementMock;

    @BeforeEach
    void setUp() throws SQLException {
        dataSourceMock = mock(DataSource.class);
        connectionMock = mock(Connection.class);
        preparedStatementMock = mock(PreparedStatement.class);

        given(dataSourceMock.getConnection()).willReturn(connectionMock);
        given(connectionMock.prepareStatement(anyString())).willReturn(preparedStatementMock);

        jdbcTemplate = new JdbcTemplate(dataSourceMock);
    }

    @Test
    void updateTest() throws SQLException {
        //given
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        String account = "account";
        String password = "pwd";
        String email = "email";

        //when
        jdbcTemplate.update(sql, account, password, email);

        //then
        then(preparedStatementMock).should(times(3)).setObject(anyInt(), any());
        then(preparedStatementMock).should(times(1)).executeUpdate();
    }

    @Test
    void queryForObjectTest() throws SQLException {
        //given
        String account = "account";
        String password = "pwd";
        String email = "email";
        String sql = "select id, account, password, email from users where id = ?";

        ResultSet resultSetMock = mock(ResultSet.class);
        RowMapper<TestUser> rowMapperMock = mock(RowMapper.class);
        given(preparedStatementMock.executeQuery()).willReturn(resultSetMock);
        given(resultSetMock.next()).willReturn(true);
        given(rowMapperMock.mapRow(eq(resultSetMock), anyInt()))
                .willReturn(new TestUser(1L, account, password, email));

        //when
        TestUser testUser = jdbcTemplate.queryForObject(sql, rowMapperMock, 1L);

        //then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(testUser.getAccount()).isEqualTo(account);
            softly.assertThat(testUser.getPassword()).isEqualTo(password);
            softly.assertThat(testUser.getEmail()).isEqualTo(email);
        });
    }

    private static class TestUser {
        private Long id;
        private String account;
        private String password;
        private String email;

        public TestUser(Long id, String account, String password, String email) {
            this.id = id;
            this.account = account;
            this.password = password;
            this.email = email;
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
