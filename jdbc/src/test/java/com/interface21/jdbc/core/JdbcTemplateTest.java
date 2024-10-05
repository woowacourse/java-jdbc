package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("JDBC 템플릿")
class JdbcTemplateTest {

    private static final RowMapper<User> USER_ROW_MAPPER = (resultSet, rowNum) -> new User(
            resultSet.getString(1),
            resultSet.getString(2),
            resultSet.getString(3)
    );

    private JdbcTemplate jdbcTemplate;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    @BeforeEach
    void setUp() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        jdbcTemplate = new JdbcTemplate(dataSource);

        when(dataSource.getConnection()).thenReturn(connection);
    }

    @DisplayName("쿼리 실행 후 RowMapper에 명시된 타입으로 결과를 반환한다.")
    @Test
    void mapByRowMapperType() throws SQLException {
        // given
        String sql = "select * from user where account = ?";

        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(1)).thenReturn("gugu@gamil.com");
        when(resultSet.getString(2)).thenReturn("gugu");
        when(resultSet.getString(3)).thenReturn("password");

        // when
        Optional<User> actual = jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, "gugu");

        // then
        assertThat(actual.get()).isInstanceOf(User.class);
    }

    @DisplayName("JDBC 템플릿은 쿼리 실행 중 SQLException이 발생하면 DataAccessException으로 잡아 처리한다.")
    @Test
    void catchExecuteQuerySqlException() throws SQLException {
        // given
        String sql = "select * from user";

        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenThrow(SQLException.class);

        // when & then
        assertThatThrownBy(() -> jdbcTemplate.query(sql, USER_ROW_MAPPER))
                .isInstanceOf(DataAccessException.class);
    }

    @DisplayName("JDBC 템플릿은 한 개의 결과 조회 시 결과가 있다면 값을 반환한다.")
    @Test
    void queryForObject() throws SQLException {
        // given
        String sql = "select * from user where account = ?";

        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(1)).thenReturn("gugu@gamil.com");
        when(resultSet.getString(2)).thenReturn("gugu");
        when(resultSet.getString(3)).thenReturn("password");

        // when
        Optional<User> actual = jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, "gugu");

        // then
        assertThat(actual.isPresent()).isTrue();
    }

    @DisplayName("JDBC 템플릿은 한 개의 결과 조회 시 결과가 없다면 null을 반환한다.")
    @Test
    void queryForObjectNull() throws SQLException {
        // given
        String sql = "select * from user where account = ?";

        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // when
        Optional<User> actual = jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, "gugu");

        // then
        assertThat(actual.isEmpty()).isTrue();
    }

    @DisplayName("JDBC 템플릿은 여러 개의 결과 조회 시 리스트를 반환한다.")
    @Test
    void query() throws SQLException {
        // given
        String sql = "select * from user";
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString(1)).thenReturn("gugu@gamil.com", "gugu2@gmail.com");
        when(resultSet.getString(2)).thenReturn("gugu", "gugu2");
        when(resultSet.getString(3)).thenReturn("password", "password2");

        // when
        List<User> actual = jdbcTemplate.query(sql, USER_ROW_MAPPER);

        // then
        assertThat(actual).hasSize(2);
    }

    private static class User {

        private final String email;
        private final String account;
        private final String password;

        private User(String email, String account, String password) {
            this.email = email;
            this.account = account;
            this.password = password;
        }
    }
}
