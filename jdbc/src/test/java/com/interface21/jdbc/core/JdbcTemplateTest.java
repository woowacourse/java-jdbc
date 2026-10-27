package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.junit.jupiter.api.AfterEach;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    @BeforeEach
    void setUp() throws SQLException {
        final DataSource dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @AfterEach
    void 사용한_자원을_해제한다() throws SQLException {
        verify(resultSet).close();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    void 단건_조회_결과가_없으면_null을_반환한다() throws SQLException {
        when(resultSet.next()).thenReturn(false);

        final String actual = jdbcTemplate.queryForObject(
                "select account from users", row -> row.getString("account"));

        assertThat(actual).isNull();
        verify(resultSet, never()).getString(anyString());
    }

    @Test
    void 단건_조회_결과가_한_건이면_객체를_반환한다() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("handa");

        final String actual = jdbcTemplate.queryForObject(
                "select account from users where id = ?", row -> row.getString("account"), 1L);

        assertThat(actual).isEqualTo("handa");
        verify(preparedStatement).setObject(1, 1L);
    }

    @Test
    void 두_번째_행이_있으면_나머지_행을_매핑하지_않고_예외를_던진다() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, true, false);
        when(resultSet.getString("account")).thenReturn("handa");

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(
                "select account from users", row -> row.getString("account")))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("조회 결과가 1건이어야 하지만 2건 이상입니다.");

        verify(resultSet, times(2)).next();
        verify(resultSet).getString("account");
    }

    @Test
    void 단건_조회_결과_매핑에_실패하면_원인_예외를_보존한다() throws SQLException {
        final SQLException failure = new SQLException("mapping failed");
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("account")).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(
                "select account from users", row -> row.getString("account")))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);
    }
}
