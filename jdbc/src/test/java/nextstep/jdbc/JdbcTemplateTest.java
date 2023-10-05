package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;

class JdbcTemplateTest {

    DataSource mockDataSource = Mockito.mock(DataSource.class);
    Connection mockConnection = Mockito.mock(Connection.class);
    PreparedStatement mockPreparedStatement = Mockito.mock(PreparedStatement.class);

    @BeforeEach
    void setUp() {
        mockDataSource = Mockito.mock(DataSource.class);
        mockConnection = Mockito.mock(Connection.class);
        mockPreparedStatement = Mockito.mock(PreparedStatement.class);
    }

    @Test
    void execute() throws SQLException {

        //given
        Mockito.when(DataSourceUtils.getConnection(mockDataSource)).thenReturn(mockConnection);
        Mockito.when(mockDataSource.getConnection()).thenReturn(mockConnection);
        Mockito.when(mockConnection.prepareStatement(Mockito.anyString())).thenReturn(mockPreparedStatement);
        Mockito.when(mockPreparedStatement.execute()).thenReturn(true);

        final var jdbcTemplate = new JdbcTemplate(mockDataSource);

        //when
        jdbcTemplate.execute("insert into user_history (user_id, account) values (?, ?)", 1L, "account");

        //then
        assertAll(
                () -> Mockito.verify(mockPreparedStatement, Mockito.times(1)).setObject(1, 1L),
                () -> Mockito.verify(mockPreparedStatement, Mockito.times(1)).setObject(2, "account"),
                () -> Mockito.verify(mockPreparedStatement, Mockito.times(1)).execute()
        );
    }

    @Test
    void query() throws SQLException {

        //given
        final var mockResultSet = Mockito.mock(ResultSet.class);

        Mockito.when(mockDataSource.getConnection()).thenReturn(mockConnection);
        Mockito.when(mockConnection.prepareStatement(Mockito.anyString())).thenReturn(mockPreparedStatement);
        Mockito.when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        Mockito.when(mockResultSet.next()).thenReturn(true).thenReturn(false);
        Mockito.when(mockResultSet.getLong(Mockito.anyString())).thenReturn(1L);
        Mockito.when(mockResultSet.getString(Mockito.anyString())).thenReturn("account").thenReturn("password").thenReturn("email");

        final var jdbcTemplate = new JdbcTemplate(mockDataSource);
        //when
        final var result = jdbcTemplate.query(
                "select * from user_history where user_id = ?",
                rs -> new User(
                        rs.getLong("user_id"),
                        rs.getString("account"),
                        rs.getString("password"),
                        rs.getString("email")
                ),
                1L
        );

        //then
        assertAll(
                () -> Mockito.verify(mockPreparedStatement, Mockito.times(1)).setObject(1, 1L),
                () -> Mockito.verify(mockPreparedStatement, Mockito.times(1)).executeQuery(),
                () -> Mockito.verify(mockResultSet, Mockito.times(2)).next(),
                () -> Mockito.verify(mockResultSet, Mockito.times(1)).getLong(Mockito.any()),
                () -> Mockito.verify(mockResultSet, Mockito.times(3)).getString(Mockito.any()),

                () -> assertThat(result).hasSize(1),
                () -> assertThat(result.get(0).getId()).isEqualTo(1L),
                () -> assertThat(result.get(0).getAccount()).isEqualTo("account"),
                () -> assertThat(result.get(0).getPassword()).isEqualTo("password"),
                () -> assertThat(result.get(0).getEmail()).isEqualTo("email")
        );
    }

    static class User {

        private final Long id;
        private final String account;
        private final String password;
        private final String email;

        public User(Long id, String account, String password, String email) {
            this.id = id;
            this.account = account;
            this.password = password;
            this.email = email;
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
