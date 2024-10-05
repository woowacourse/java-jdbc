package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class JdbcTemplateTest {

    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() throws SQLException {
        DataSource dataSource = Mockito.mock(DataSource.class);
        connection = Mockito.mock(Connection.class);
        preparedStatement = Mockito.mock(PreparedStatement.class);
        resultSet = Mockito.mock(ResultSet.class);

        given(dataSource.getConnection()).willReturn(connection);
        given(connection.prepareStatement(any())).willReturn(preparedStatement);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @AfterEach
    void tearDown() {
        assertAll(
                () -> verify(connection).close(),
                () -> verify(preparedStatement).close()
        );
    }

    @DisplayName("update시 전달받은 인자를 statement에 연결해준다.")
    @Test
    void settingParameter() {
        String sql = "INSERT INTO users (account, password, email) VALUES (?, ?, ?)";
        String account = "reviewer";
        String password = "atto";
        String email = "WTC6th@com";

        jdbcTemplate.update(sql, account, password, email);

        assertAll(
                () -> verify(preparedStatement).setObject(1, account),
                () -> verify(preparedStatement).setObject(2, password),
                () -> verify(preparedStatement).setObject(3, email)
        );
    }

    @DisplayName("mapper가 올바르게 동작한다.")
    @Test
    void mapping() throws SQLException {
        String sql = "SELECT id, account, password, email FROM users WHERE id = ?";
        Long id = 2L;
        String account = "daon";
        given(preparedStatement.executeQuery()).willReturn(resultSet);
        given(resultSet.next()).willReturn(true);
        given(resultSet.getLong("id")).willReturn(id);
        given(resultSet.getString("account")).willReturn(account);

        RowMapper<TestObject> mapper = result -> new TestObject(
                result.getLong("id"),
                result.getString("account")
        );

        Optional<TestObject> testObject = jdbcTemplate.queryForObject(sql, mapper, id);

        assertAll(
                () -> assertThat(testObject).isPresent(),
                () -> assertThat(testObject).hasValueSatisfying(result -> assertAll(
                        () -> assertThat(result.id).isEqualTo(id),
                        () -> assertThat(result.account).isEqualTo(account)
                ))
        );
    }

    @DisplayName("checked 예외를 unchecked 예외로 바꿔준다.")
    @Test
    void reThrow() throws SQLException {
        given(preparedStatement.executeUpdate()).willThrow(SQLException.class);

        assertThatThrownBy(() -> jdbcTemplate.update("BAD SQL GRAMMAR"))
                .isInstanceOf(DataAccessException.class);
    }


    private record TestObject(Long id, String account) {
    }
}
