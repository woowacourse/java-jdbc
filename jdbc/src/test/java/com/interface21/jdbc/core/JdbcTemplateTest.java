package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.EmptyResultDataAccessException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

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
    void update는_PreparedStatementSetter로_값을_바인딩한다() throws SQLException {
        jdbcTemplate.update("update users set password = ? where id = ?", pstmt -> {
            pstmt.setString(1, "password99");
            pstmt.setLong(2, 1L);
        });

        final InOrder inOrder = inOrder(preparedStatement);
        inOrder.verify(preparedStatement).setString(1, "password99");
        inOrder.verify(preparedStatement).setLong(2, 1L);
        inOrder.verify(preparedStatement).executeUpdate();
    }

    @Test
    void PreparedStatementSetter에서_SQLException이_나면_DataAccessException으로_바꾸고_자원을_닫는다() throws SQLException {
        final var cause = new SQLException("invalid parameter index");

        assertThatThrownBy(() -> jdbcTemplate.update("update users set password = ?", pstmt -> {
            throw cause;
        }))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);
        verify(preparedStatement).close();
        verify(connection).close();
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
    void 스레드에_묶인_Connection이_있으면_그_Connection으로_실행하고_닫지_않는다() throws SQLException {
        final var boundConnection = mock(Connection.class);
        given(boundConnection.prepareStatement(anyString())).willReturn(preparedStatement);
        TransactionSynchronizationManager.bindResource(dataSource, boundConnection);

        try {
            jdbcTemplate.update("update users set password = ? where id = ?", "password99", 1L);

            verify(dataSource, never()).getConnection();
            verify(preparedStatement).executeUpdate();
            verify(preparedStatement).close();
            verify(boundConnection, never()).close();
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    @Test
    void Connection을_얻지_못하면_DataAccessException을_던진다() throws SQLException {
        final var cause = new SQLException("connection refused");
        given(dataSource.getConnection()).willThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.update("delete from users"))
                .isInstanceOf(CannotGetJdbcConnectionException.class)
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);
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
    void query는_PreparedStatementSetter로_값을_바인딩한다() throws SQLException {
        given(resultSet.next()).willReturn(true, false);
        given(resultSet.getString("account")).willReturn("gugu");

        final var accounts = jdbcTemplate.query(
                "select account from users where id = ?",
                pstmt -> pstmt.setLong(1, 1L),
                (rs, rowNum) -> rs.getString("account"));

        assertThat(accounts).containsExactly("gugu");
        final InOrder inOrder = inOrder(preparedStatement);
        inOrder.verify(preparedStatement).setLong(1, 1L);
        inOrder.verify(preparedStatement).executeQuery();
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

    @Test
    void queryForOptional은_결과가_한_건이면_값을_담아_반환한다() throws SQLException {
        given(resultSet.next()).willReturn(true, false);
        given(resultSet.getString("account")).willReturn("gugu");

        final var account = jdbcTemplate.queryForOptional(
                "select account from users where id = ?", (rs, rowNum) -> rs.getString("account"), 1L);

        assertThat(account).contains("gugu");
    }

    @Test
    void queryForOptional은_결과가_없으면_빈_Optional을_반환한다() throws SQLException {
        given(resultSet.next()).willReturn(false);

        final var account = jdbcTemplate.queryForOptional(
                "select account from users where id = ?", (rs, rowNum) -> rs.getString("account"), 1L);

        assertThat(account).isEmpty();
        verify(resultSet).close();
        verify(connection).close();
    }

    @Test
    void queryForOptional은_결과가_두_건_이상이면_IncorrectResultSizeDataAccessException을_던진다() throws SQLException {
        given(resultSet.next()).willReturn(true, true, false);

        assertThatThrownBy(() -> jdbcTemplate.queryForOptional(
                "select account from users where account = ?", (rs, rowNum) -> rs.getString("account"), "gugu"))
                .isExactlyInstanceOf(IncorrectResultSizeDataAccessException.class)
                .extracting("actualSize")
                .isEqualTo(2);
    }
}
