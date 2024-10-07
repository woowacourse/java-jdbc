package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private final DataSource dataSource = mock();
    private final Connection connection = mock();
    private final PreparedStatement preparedStatement = mock();
    private final ResultSet resultSet = mock();

    private JdbcTemplate sut;

    @BeforeEach
    void setup() throws SQLException {
        given(dataSource.getConnection()).willReturn(connection);
        given(connection.prepareStatement(anyString())).willReturn(preparedStatement);
        given(preparedStatement.getConnection()).willReturn(connection);
        given(preparedStatement.executeQuery()).willReturn(resultSet);

        sut = new JdbcTemplate(dataSource);
    }

    @Test
    void queryForObject() throws SQLException {
        // given
        final var sql = "select * from users where id = ?";
        final var saved = new User(1L, "gugu");

        given(resultSet.next()).willReturn(true);
        given(resultSet.getLong(1)).willReturn(saved.id);
        given(resultSet.getString(2)).willReturn(saved.account);

        // when
        final var actual = sut.queryForObject(sql, userRowMapper, 1L);

        // then
        assertThat(actual.id).isEqualTo(saved.id);
        assertThat(actual.account).isEqualTo(saved.account);
        verify(connection).close();
        verify(preparedStatement).close();
        verify(resultSet).close();
    }

    @Test
    void queryForList() throws SQLException {
        // given
        final var sql = "select * from users";
        final var saved1 = new User(1L, "gugu");
        final var saved2 = new User(2L, "wonny");
        final var saved3 = new User(3L, "lisa");

        given(resultSet.next()).willReturn(true, true, true, false);
        given(resultSet.getLong(1)).willReturn(saved1.id, saved2.id, saved3.id);
        given(resultSet.getString(2)).willReturn(saved1.account, saved2.account, saved3.account);

        // when
        final var actual = sut.queryForList(sql, userRowMapper);

        // then
        assertThat(actual)
                .hasSize(3)
                .containsExactly(saved1, saved2, saved3);
        verify(connection).close();
        verify(preparedStatement).close();
        verify(resultSet).close();
    }

    @Test
    void update() throws SQLException {
        // given
        final var sql = "update users account = ? where id = ?";
        final var updated = new User(1L, "left hand");

        // when
        sut.update(sql, updated.account, updated.id);

        // then
        verify(preparedStatement).setObject(1, updated.account);
        verify(preparedStatement).setObject(2, updated.id);
        verify(connection).close();
        verify(preparedStatement).close();
    }

    private record User(Long id, String account) {}

    private final RowMapper<User> userRowMapper = resultSet -> new User(
            resultSet.getLong("id"),
            resultSet.getString("account"));
}
