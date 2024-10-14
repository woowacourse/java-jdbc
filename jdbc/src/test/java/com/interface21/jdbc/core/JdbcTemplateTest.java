package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.DataSizeNotMatchedException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class JdbcTemplateTest {

    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() throws SQLException {
        DataSource dataSource = Mockito.mock(DataSource.class);
        Connection connection = Mockito.mock(Connection.class);
        preparedStatement = Mockito.mock(PreparedStatement.class);
        resultSet = Mockito.mock(ResultSet.class);

        given(dataSource.getConnection()).willReturn(connection);
        given(connection.prepareStatement(any(), anyInt())).willReturn(preparedStatement);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @AfterEach
    void tearDown() throws SQLException {
        verify(preparedStatement).close();
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

    @DisplayName("mapper로 쿼리 결과를 객체로 변환한다.")
    @Test
    void mapping() throws SQLException {
        String sql = "SELECT id, account FROM users WHERE id = ?";
        Long id = 2L;
        String account = "daon";
        given(preparedStatement.executeQuery()).willReturn(resultSet);
        given(resultSet.next()).willReturn(true).willReturn(false);
        given(resultSet.getLong("id")).willReturn(id);
        given(resultSet.getString("account")).willReturn(account);

        RowMapper<TestObject> mapper = result -> new TestObject(
                result.getLong("id"),
                result.getString("account")
        );

        TestObject result = jdbcTemplate.queryForObject(sql, mapper, id);

        assertAll(
                () -> assertThat(result.id).isEqualTo(id),
                () -> assertThat(result.account).isEqualTo(account)
        );
    }

    @DisplayName("단 건 조회 로직의 결과가 한 건이 아닌 경우 예외가 발생한다.")
    @Test
    void throwsQueryForObjectWhenIllegalQuerySize() throws SQLException {
        String sql = "SELECT id, account FROM users WHERE id = ?";
        Long id = 2L;
        given(preparedStatement.executeQuery()).willReturn(resultSet);
        given(resultSet.next()).willReturn(true).willReturn(true);
        RowMapper<TestObject> mapper = result -> new TestObject(
                result.getLong("id"),
                result.getString("account")
        );

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, mapper, id))
                .isInstanceOf(DataSizeNotMatchedException.class);
    }

    @DisplayName("checked 예외를 unchecked 예외로 바꿔준다.")
    @Test
    void reThrow() throws SQLException {
        given(preparedStatement.executeUpdate()).willThrow(SQLException.class);

        assertThatThrownBy(() -> jdbcTemplate.update("BAD SQL GRAMMAR"))
                .isInstanceOf(DataAccessException.class)
                .hasCauseInstanceOf(SQLException.class);
    }

    private record TestObject(Long id, String account) {
    }
}
