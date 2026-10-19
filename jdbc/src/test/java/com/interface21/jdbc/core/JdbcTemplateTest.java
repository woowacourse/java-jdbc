package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class JdbcTemplateTest {

    private static final RowMapper<String> ROW_MAPPER = rs -> rs.getString("account");

    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        final DataSource dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        given(dataSource.getConnection()).willReturn(connection);
        given(connection.prepareStatement(anyString())).willReturn(preparedStatement);
        given(preparedStatement.executeQuery()).willReturn(resultSet);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    @DisplayName("update는 인자를 순서대로 바인딩한다")
    void updateBindsArgumentsInOrder() throws SQLException {
        jdbcTemplate.update("insert into users (account, password) values (?, ?)", "gugu", "password");

        final InOrder inOrder = inOrder(preparedStatement);
        inOrder.verify(preparedStatement).setObject(1, "gugu");
        inOrder.verify(preparedStatement).setObject(2, "password");
        inOrder.verify(preparedStatement).executeUpdate();
    }

    @Test
    @DisplayName("PreparedStatementSetter를 넘기면 사용자가 정한 방식으로 바인딩한다")
    void updateWithPreparedStatementSetter() throws SQLException {
        jdbcTemplate.update("insert into users (account, email) values (?, ?)", pstmt -> {
            pstmt.setString(1, "gugu");
            pstmt.setNull(2, Types.VARCHAR);
        });

        final InOrder inOrder = inOrder(preparedStatement);
        inOrder.verify(preparedStatement).setString(1, "gugu");
        inOrder.verify(preparedStatement).setNull(2, Types.VARCHAR);
        inOrder.verify(preparedStatement).executeUpdate();
    }

    @Test
    @DisplayName("update는 영향받은 행 수를 반환한다")
    void updateReturnsAffectedRows() throws SQLException {
        given(preparedStatement.executeUpdate()).willReturn(3);

        final int updatedRows = jdbcTemplate.update("delete from users");

        assertThat(updatedRows).isEqualTo(3);
    }

    @Test
    @DisplayName("실행이 끝나면 PreparedStatement와 Connection을 닫는다")
    void closesResourcesAfterExecution() throws SQLException {
        jdbcTemplate.update("delete from users");

        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("SQLException이 발생하면 DataAccessException으로 바꿔 던진다")
    void translatesSQLException() throws SQLException {
        given(preparedStatement.executeUpdate()).willThrow(new SQLException("제약조건 위반"));

        assertThatThrownBy(() -> jdbcTemplate.update("delete from users"))
                .isInstanceOf(DataAccessException.class)
                .hasCauseInstanceOf(SQLException.class);
    }

    @Test
    @DisplayName("예외가 발생해도 PreparedStatement와 Connection을 닫는다")
    void closesResourcesWhenExceptionOccurs() throws SQLException {
        given(preparedStatement.executeUpdate()).willThrow(new SQLException("제약조건 위반"));

        assertThatThrownBy(() -> jdbcTemplate.update("delete from users"))
                .isInstanceOf(DataAccessException.class);

        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("query는 모든 행을 매핑하고 ResultSet을 닫는다")
    void queryMapsAllRows() throws SQLException {
        given(resultSet.next()).willReturn(true, true, false);
        given(resultSet.getString("account")).willReturn("gugu", "tion");

        final List<String> accounts = jdbcTemplate.query("select account from users", ROW_MAPPER);

        assertThat(accounts).containsExactly("gugu", "tion");
        verify(resultSet).close();
    }

    @Test
    @DisplayName("queryForObject는 결과가 1건이면 값을 담아 반환한다")
    void queryForObjectReturnsSingleResult() throws SQLException {
        given(resultSet.next()).willReturn(true, false);
        given(resultSet.getString("account")).willReturn("gugu");

        final Optional<String> account = jdbcTemplate.queryForObject(
                "select account from users where id = ?", ROW_MAPPER, 1L);

        assertThat(account).contains("gugu");
        verify(preparedStatement).setObject(1, 1L);
    }

    @Test
    @DisplayName("queryForObject는 결과가 0건이면 빈 Optional을 반환한다")
    void queryForObjectReturnsEmptyWhenNoResult() throws SQLException {
        given(resultSet.next()).willReturn(false);

        final Optional<String> account = jdbcTemplate.queryForObject(
                "select account from users where id = ?", ROW_MAPPER, 1L);

        assertThat(account).isEmpty();
    }

    @Test
    @DisplayName("queryForObject는 결과가 2건 이상이면 예외를 던지고 자원을 닫는다")
    void queryForObjectThrowsWhenMultipleResults() throws SQLException {
        given(resultSet.next()).willReturn(true, true);
        given(resultSet.getString("account")).willReturn("gugu", "tion");

        assertThatThrownBy(() -> jdbcTemplate.queryForObject("select account from users", ROW_MAPPER))
                .isInstanceOf(DataAccessException.class);

        verify(resultSet).close();
        verify(preparedStatement).close();
        verify(connection).close();
    }
}
