package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class JdbcTemplateTest {

    private final RowMapper<String> accountMapper = resultSet -> resultSet.getString("account");

    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        final var dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        given(dataSource.getConnection()).willReturn(connection);
        given(connection.prepareStatement(anyString())).willReturn(preparedStatement);
        given(preparedStatement.executeQuery()).willReturn(resultSet);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void update는_인자를_순서대로_바인딩하고_변경된_행_수를_반환한다() throws SQLException {
        given(preparedStatement.executeUpdate()).willReturn(1);

        final var updatedRows = jdbcTemplate.update(
                "update users set password = ? where id = ?", "password", 1L);

        assertThat(updatedRows).isEqualTo(1);
        verify(preparedStatement).setObject(1, "password");
        verify(preparedStatement).setObject(2, 1L);
    }

    @Test
    void query는_모든_행을_RowMapper로_변환한다() throws SQLException {
        given(resultSet.next()).willReturn(true, true, false);
        given(resultSet.getString("account")).willReturn("gugu", "ludens");

        final var accounts = jdbcTemplate.query("select account from users", accountMapper);

        assertThat(accounts).containsExactly("gugu", "ludens");
    }

    @Test
    void queryForObject는_한_행을_RowMapper로_변환한다() throws SQLException {
        given(resultSet.next()).willReturn(true, false);
        given(resultSet.getString("account")).willReturn("gugu");

        final var account = jdbcTemplate.queryForObject(
                "select account from users where id = ?", accountMapper, 1L);

        assertThat(account).isEqualTo("gugu");
        verify(preparedStatement).setObject(1, 1L);
    }

    @Test
    void queryForObject는_조회_결과가_없으면_예외가_발생한다() throws SQLException {
        given(resultSet.next()).willReturn(false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(
                "select account from users where id = ?", accountMapper, 1L))
                .isInstanceOf(IncorrectResultSizeDataAccessException.class)
                .hasMessageContaining("없습니다");
    }

    @Test
    void queryForObject는_두_번째_행이_있으면_더_매핑하지_않고_예외가_발생한다() throws SQLException {
        given(resultSet.next()).willReturn(true, true, true, false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(
                "select account from users where account = ?", accountMapper, "gugu"))
                .isInstanceOf(IncorrectResultSizeDataAccessException.class)
                .hasMessageContaining("2건 이상");
        verify(resultSet, times(2)).next();
        verify(resultSet, times(1)).getString("account");
    }

    @Test
    void 실행이_끝나면_사용한_자원을_모두_닫는다() throws SQLException {
        given(resultSet.next()).willReturn(false);

        jdbcTemplate.query("select account from users", accountMapper);

        verify(resultSet).close();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    void SQLException은_DataAccessException으로_변환하고_자원을_닫는다() throws SQLException {
        final var cause = new SQLException("syntax error");
        given(preparedStatement.executeUpdate()).willThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.update("update users set"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);
        verify(preparedStatement).close();
        verify(connection).close();
    }
}
