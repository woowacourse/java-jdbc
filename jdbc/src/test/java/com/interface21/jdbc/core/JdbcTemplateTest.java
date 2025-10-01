package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class JdbcTemplateTest {

    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;

    private record TestUser(
            String account,
            String password,
            String email
    ) {
    }

    private final RowMapper<TestUser> userRowMapper = (rs) -> new TestUser(
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email")
    );

    @BeforeEach
    void setUp() throws SQLException {
        connection = Mockito.mock(Connection.class);
        preparedStatement = Mockito.mock(PreparedStatement.class);
        resultSet = Mockito.mock(ResultSet.class);

        final DataSource dataSource = Mockito.mock(DataSource.class);
        jdbcTemplate = new JdbcTemplate(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
    }

    @DisplayName("update는 PreparedStatement에 파라미터를 설정하고 업데이트를 실행한다.")
    @Test
    void update() throws SQLException {
        // given
        final String sql = """
                UPDATE users
                SET password = ?
                WHERE account = ?
                """;
        final String newPassword = "new_password";
        final String account = "gugu";
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);

        // when
        jdbcTemplate.update(sql, newPassword, account);

        // then
        assertAll(
                () -> verify(preparedStatement).setObject(1, newPassword),
                () -> verify(preparedStatement).setObject(2, account),
                () -> verify(preparedStatement).executeUpdate()
        );
    }

    @DisplayName("insert는 PreparedStatement에 파라미터를 설정하고 업데이트를 실행한다.")
    @Test
    void insert() throws SQLException {
        // given
        final String sql = """
                INSERT INTO users (account, password, email)
                VALUES (?, ?, ?)
                """;
        final TestUser user = new TestUser("gugu", "password", "gugu@email.com");
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);

        // when
        jdbcTemplate.insert(sql, user.account, user.password, user.email);

        // then
        assertAll(
                () -> verify(preparedStatement).setObject(1, user.account),
                () -> verify(preparedStatement).setObject(2, user.password),
                () -> verify(preparedStatement).setObject(3, user.email),
                () -> verify(preparedStatement).executeUpdate()
        );
    }

    @DisplayName("findAll은 ResultSet을 RowMapper를 통해 객체 리스트로 변환한다.")
    @Test
    void findAll() throws SQLException {
        // given
        final String sql = """
                SELECT account, password, email
                FROM users
                """;
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "joo");
        when(resultSet.getString("password")).thenReturn("pw1", "pw2");
        when(resultSet.getString("email")).thenReturn("gugu@email.com", "joo@email.com");

        // when
        final List<TestUser> users = jdbcTemplate.findAll(sql, userRowMapper);

        // then
        assertAll(
                () -> assertThat(users).hasSize(2),
                () -> assertThat(users.get(0).account).isEqualTo("gugu"),
                () -> assertThat(users.get(1).account).isEqualTo("joo")
        );
    }

    @DisplayName("findById는 단일 객체를 정확히 매핑하여 반환한다.")
    @Test
    void findById() throws SQLException {
        // given
        final String sql = """
                SELECT account, password, email
                FROM users
                WHERE account = ?
                """;
        final TestUser user = new TestUser("gugu", "password", "gugu@email.com");
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn(user.account);
        when(resultSet.getString("password")).thenReturn(user.password);
        when(resultSet.getString("email")).thenReturn(user.email);

        // when
        final TestUser foundUser = jdbcTemplate.findById(sql, userRowMapper, user.account);

        // then
        assertAll(
                () -> verify(preparedStatement).setObject(1, user.account),
                () -> assertThat(foundUser).isNotNull(),
                () -> assertThat(foundUser.account).isEqualTo(user.account)
        );
    }

    @DisplayName("queryForObject는 단일 값을 반환한다.")
    @Test
    void queryForObject() throws SQLException {
        // given
        final String sql = """
                SELECT COUNT(*)
                FROM users
                """;
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getLong(1)).thenReturn(1L);

        // when
        final Long count = jdbcTemplate.queryForObject(sql, rs -> rs.getLong(1));

        // then
        assertThat(count).isEqualTo(1L);
    }

    @DisplayName("SQLException 발생 시 DataAccessException으로 전환하여 던진다.")
    @Test
    void throwDataAccessExceptionOnSqlException() throws SQLException {
        // given
        final String sql = """
                SELECT *
                FROM users
                """;
        when(connection.prepareStatement(sql)).thenThrow(new SQLException("Test Exception"));

        // when & then
        assertThatThrownBy(() -> jdbcTemplate.findAll(sql, userRowMapper))
                .isInstanceOf(DataAccessException.class);
    }
}
