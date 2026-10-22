package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.EmptyResultDataAccessException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class JdbcTemplateTest {

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

        final var updated = jdbcTemplate.update("insert into users (account, password) values (?, ?)", "gugu", "password");

        assertThat(updated).isEqualTo(1);
        final InOrder inOrder = inOrder(preparedStatement);
        inOrder.verify(preparedStatement).setObject(1, "gugu");
        inOrder.verify(preparedStatement).setObject(2, "password");
        inOrder.verify(preparedStatement).executeUpdate();
    }

    @Test
    void update가_끝나면_PreparedStatement와_Connection을_닫는다() throws SQLException {
        jdbcTemplate.update("delete from users");

        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    void update_중_SQLException이_나면_DataAccessException으로_바꾸고_자원을_닫는다() throws SQLException {
        final var cause = new SQLException("syntax error");
        given(preparedStatement.executeUpdate()).willThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.update("wrong sql"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    void query는_모든_행을_RowMapper로_변환하고_rowNum을_0부터_넘긴다() throws SQLException {
        given(resultSet.next()).willReturn(true, true, false);
        given(resultSet.getString("account")).willReturn("gugu", "kirby");
        final List<Integer> rowNums = new ArrayList<>();

        final var accounts = jdbcTemplate.query("select account from users", (rs, rowNum) -> {
            rowNums.add(rowNum);
            return rs.getString("account");
        });

        assertThat(accounts).containsExactly("gugu", "kirby");
        assertThat(rowNums).containsExactly(0, 1);
    }

    @Test
    void query가_끝나면_ResultSet과_PreparedStatement와_Connection을_닫는다() throws SQLException {
        given(resultSet.next()).willReturn(false);

        jdbcTemplate.query("select account from users where id = ?", (rs, rowNum) -> rs.getString("account"), 1L);

        verify(preparedStatement).setObject(1, 1L);
        verify(resultSet).close();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    void queryForObject는_결과가_한_건이면_그_객체를_반환한다() throws SQLException {
        given(resultSet.next()).willReturn(true, false);
        given(resultSet.getString("account")).willReturn("gugu");

        final var account = jdbcTemplate.queryForObject(
                "select account from users where id = ?", (rs, rowNum) -> rs.getString("account"), 1L);

        assertThat(account).isEqualTo("gugu");
    }

    @Test
    void queryForObject는_결과가_없으면_EmptyResultDataAccessException을_던진다() throws SQLException {
        given(resultSet.next()).willReturn(false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(
                "select account from users where id = ?", (rs, rowNum) -> rs.getString("account"), 1L))
                .isInstanceOf(EmptyResultDataAccessException.class);
        verify(resultSet).close();
        verify(connection).close();
    }

    @Test
    void queryForObject는_결과가_두_건_이상이면_IncorrectResultSizeDataAccessException을_던진다() throws SQLException {
        given(resultSet.next()).willReturn(true, true, false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(
                "select account from users where account = ?", (rs, rowNum) -> rs.getString("account"), "gugu"))
                .isExactlyInstanceOf(IncorrectResultSizeDataAccessException.class)
                .extracting("actualSize")
                .isEqualTo(2);
    }
}
