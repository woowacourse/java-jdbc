package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class JdbcTemplateTest {

    private Connection connection;
    private PreparedStatement preparedStatement;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        DataSource dataSource = Mockito.mock(DataSource.class);
        connection = Mockito.mock(Connection.class);
        preparedStatement = Mockito.mock(PreparedStatement.class);
        Mockito.when(dataSource.getConnection()).thenReturn(connection);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Nested
    @DisplayName("update 메서드는")
    class Update {

        @Test
        @DisplayName("INSERT 쿼리를 처리할 수 있다.")
        void success_insert_only() throws SQLException {
            User user = new User("leo", "password");
            String sql = String.format(
                "insert into users (account, password) values (%s, %s)",
                user.getAccount(), user.getPassword());

            Mockito.when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
            jdbcTemplate.update(sql);

            Mockito.verify(preparedStatement).executeUpdate();
        }

        @Test
        @DisplayName("파라미터가 있는 INSERT 쿼리를 처리할 수 있다.")
        void success_insert_parameters() throws SQLException {
            User user = new User("leo", "password");
            List<Object> values = new ArrayList<>();
            values.add(user.getAccount());
            values.add(user.getPassword());

            String sql = "insert into users (account, password) values (?, ?)";

            Mockito.when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
            jdbcTemplate.update(sql, values);

            Mockito.verify(preparedStatement).setObject(1, "leo");
            Mockito.verify(preparedStatement).setObject(2, "password");
            Mockito.verify(preparedStatement).executeUpdate();
        }
    }


    public static class User {

        private final String account;
        private final String password;

        public User(String account, String password) {
            this.account = account;
            this.password = password;
        }

        public String getAccount() {
            return account;
        }

        public String getPassword() {
            return password;
        }
    }
}
