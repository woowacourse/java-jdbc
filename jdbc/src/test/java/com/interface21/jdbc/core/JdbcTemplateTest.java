package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        given(dataSource.getConnection()).willReturn(connection);
        given(connection.prepareStatement(anyString())).willReturn(preparedStatement);
        given(preparedStatement.executeQuery()).willReturn(resultSet);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @DisplayName("update")
    @Test
    void update() throws SQLException {
        String sql = "update users set account = ?, password = ?, email = ? where id = ?";

        final Object[] objects = new Object[]{"changeAccount", "changePassword", "changeEmail", 1L};

        PreparedStatementSetter setter = pstmt -> {
            for (int i = 0; i < objects.length; i++) {
                pstmt.setObject(i + 1, objects[i]);
            }
        };
        jdbcTemplate.update(sql, setter);

        assertAll(
                () -> then(connection).should().prepareStatement(sql),
                () -> then(preparedStatement).should().setObject(1, "changeAccount"),
                () -> then(preparedStatement).should().setObject(2, "changePassword"),
                () -> then(preparedStatement).should().setObject(3, "changeEmail"),
                () -> then(preparedStatement).should().setObject(4, 1L),
                () -> then(preparedStatement).should().executeUpdate(),
                () -> then(preparedStatement).should().close(),
                () -> then(dataSource).should().getConnection()
        );
    }

    @DisplayName("queryForObject")
    @Test
    void queryForObject() throws SQLException {
        given(preparedStatement.executeQuery()).willReturn(resultSet);
        given(resultSet.getRow()).willReturn(1);
        given(resultSet.next()).willReturn(true);
        given(resultSet.getLong("id")).willReturn(1L);
        given(resultSet.getString("account")).willReturn("account");
        given(resultSet.getString("password")).willReturn("password");
        given(resultSet.getString("email")).willReturn("email");

        String sql = "select * from users where id = ?";

        RowMapper<User> rowMapper = (resultSet, num) -> new User(
                resultSet.getLong("id"),
                resultSet.getString("account"),
                resultSet.getString("password"),
                resultSet.getString("email"));

        PreparedStatementSetter preparedStatementSetter = pstmt -> pstmt.setObject(1, 1);

        User user = jdbcTemplate.queryForObject(sql, rowMapper, preparedStatementSetter);

        assertAll(
                () -> then(connection).should().prepareStatement(sql),
                () -> then(preparedStatement).should().executeQuery(),
                () -> then(resultSet).should().next(),
                () -> then(resultSet).should().getLong("id"),
                () -> then(resultSet).should().getString("account"),
                () -> then(resultSet).should().getString("password"),
                () -> then(resultSet).should().getString("email"),
                () -> then(preparedStatement).should().close(),
                () -> then(dataSource).should().getConnection(),
                () -> assertThat(user).isEqualTo(new User(1L, "account", "password", "email"))
        );
    }

    @DisplayName("query")
    @Test
    void query() throws SQLException {
        String sql = "select * from users";

        given(preparedStatement.executeQuery()).willReturn(resultSet);
        given(resultSet.next()).willReturn(true, true, false);

        RowMapper<User> rowMapper = (resultSet, rowNum) -> new User(
                resultSet.getLong("id"),
                resultSet.getString("account"),
                resultSet.getString("password"),
                resultSet.getString("email"));

        List<User> users = jdbcTemplate.query(sql, rowMapper, pstmt -> {});

        assertAll(
                () -> then(connection).should().prepareStatement(sql),
                () -> then(preparedStatement).should().executeQuery(),
                () -> then(resultSet).should(times(3)).next(),
                () -> assertThat(users.size()).isEqualTo(2)
        );
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

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            User user = (User) o;
            return Objects.equals(id, user.id) && Objects.equals(account, user.account)
                    && Objects.equals(password, user.password) && Objects.equals(email, user.email);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, account, password, email);
        }
    }
}
