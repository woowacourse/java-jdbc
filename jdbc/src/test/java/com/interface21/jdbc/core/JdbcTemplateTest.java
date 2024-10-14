package com.interface21.jdbc.core;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private final DataSource dataSource = mock();
    private final Connection connection = mock();
    private final PreparedStatement preparedStatement = mock();
    private final ResultSet resultSet = mock();

    @BeforeEach
    void setup() throws Exception {
        given(this.dataSource.getConnection()).willReturn(this.connection);
        given(this.connection.prepareStatement(anyString())).willReturn(this.preparedStatement);
        given(this.preparedStatement.executeQuery()).willReturn(this.resultSet);
        given(this.preparedStatement.executeQuery(anyString())).willReturn(this.resultSet);
        given(this.preparedStatement.getConnection()).willReturn(this.connection);
    }

    @DisplayName("update 실행 중 예외가 발생해도 관련된 리소스들이 닫혀야 한다.")
    @Test
    void updateWithException() throws SQLException {
        // given
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        PreparedStatementSetter preparedStatementSetter = (preparedStatement) -> {
            preparedStatement.setString(1, "pororo");
            preparedStatement.setString(2, "poke");
        };
        SQLException sqlException = new SQLException("업데이트 중 예외 발생");
        given(this.preparedStatement.executeUpdate()).willThrow(sqlException);

        // when
        assertThatExceptionOfType(DataAccessException.class)
                .isThrownBy(() -> jdbcTemplate.update("update error", preparedStatementSetter))
                .withCause(sqlException);

        // then
        verify(connection).close();
        verify(preparedStatement).close();
        verifyNoInteractions(resultSet);
    }

    @DisplayName("업데이트 쿼리문에 여러 파라미터를 넣을 때 파라미터가 순서되로 설정된다.")
    @Test
    void updateWithMultipleParameter() throws SQLException {
        // given
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        PreparedStatementSetter preparedStatementSetter = (preparedStatement) -> {
            preparedStatement.setString(1, "account");
            preparedStatement.setString(2, "password");
            preparedStatement.setString(3, "email");
        };

        // when
        jdbcTemplate.update("insert into user (account, age, email) values (?, ?, ?)", preparedStatementSetter);

        // then
        verify(preparedStatement).close();
        verify(connection).close();
        verifyNoInteractions(resultSet);
    }

    @DisplayName("업데이트 쿼리문에 파라미터를 넣지 않을 경우 setObject를 호출하지 않는다.")
    @Test
    void updateWithNoParameter() throws SQLException {
        // given
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        PreparedStatementSetter preparedStatementSetter = (preparedStatement) -> {
        };

        // when
        jdbcTemplate.update("insert into user (account, age, email) values ('pororo', 20, 'proro@zzang.com')",
                preparedStatementSetter);

        // then
        verify(preparedStatement, times(0)).setObject(anyInt(), any());
        verify(preparedStatement).close();
        verify(connection).close();
        verifyNoInteractions(resultSet);
    }

    @DisplayName("파라미터로 받은 connection을 이용해 update 실행할 때 connection은 close되지 않는다.")
    @Test
    void updateWithConnectionParameter() throws SQLException {
        // given
        Connection externalConnection = mock(Connection.class);
        PreparedStatement externalPreparedStatement = mock(PreparedStatement.class);
        given(externalConnection.prepareStatement(anyString())).willReturn(externalPreparedStatement);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        PreparedStatementSetter preparedStatementSetter = (preparedStatement) -> {
        };

        // when
        jdbcTemplate.update("insert into user (account, age, email) values ('pororo', 20, 'proro@zzang.com')",
                externalConnection,
                preparedStatementSetter);

        // then
        verify(externalPreparedStatement, times(0)).setObject(anyInt(), any());
        verify(externalPreparedStatement).close();
        verify(externalConnection, times(0)).close();
        verifyNoInteractions(resultSet);
    }


    @DisplayName("query 내에 쿼리문 실행 중 예외가 발생할 경우 관련된 리소스들이 닫혀야 한다.")
    @Test
    void queryWithExecuteQueryException() throws SQLException {
        // given
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        PreparedStatementSetter preparedStatementSetter = (preparedStatement) -> {
            preparedStatement.setString(1, "pororo");
            preparedStatement.setString(2, "poke");
        };
        SQLException sqlException = new SQLException("조회 중 예외 발생");
        RowMapper<?> mapper = mock();
        given(this.preparedStatement.executeQuery()).willThrow(sqlException);

        // when
        assertThatExceptionOfType(DataAccessException.class)
                .isThrownBy(() -> jdbcTemplate.query("select error", preparedStatementSetter, mapper))
                .withCause(sqlException);

        // then
        verify(connection).close();
        verify(preparedStatement).close();
        verifyNoInteractions(resultSet);
    }

    @DisplayName("query 내에 row mapping 도중 예외가 발생할 경우 관련된 리소스들이 닫혀야 한다.")
    @Test
    void queryWithRowMapException() throws SQLException {
        // given
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        PreparedStatementSetter preparedStatementSetter = (preparedStatement) -> {
            preparedStatement.setString(1, "pororo");
            preparedStatement.setString(2, "poke");
        };
        SQLException sqlException = new SQLException("map 중 예외 발생");
        RowMapper<?> mapper = mock();
        given(this.resultSet.next()).willThrow(sqlException);

        // when
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> jdbcTemplate.query("select error", preparedStatementSetter, mapper))
                .withCause(sqlException);

        // then
        verify(connection).close();
        verify(preparedStatement).close();
        verify(resultSet).close();
    }

    @DisplayName("조회 쿼리문에 여러 파라미터를 넣을 때 파라미터가 순서되로 설정된다.")
    @Test
    void queryWithMultipleParameter() throws SQLException {
        // given
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        PreparedStatementSetter preparedStatementSetter = (preparedStatement) -> {
            preparedStatement.setInt(1, 1);
            preparedStatement.setString(2, "pororo");
            preparedStatement.setString(3, "pororo@zzang.com");
        };
        RowMapper<?> mapper = mock();

        // when
        jdbcTemplate.query("select * from user where id = ? and account = ? and email = ?", preparedStatementSetter,
                mapper);

        // then
        verify(preparedStatement).close();
        verify(connection).close();
        verify(resultSet).close();
    }


    @DisplayName("조회 쿼리문에 파라미터가 설정되어 있지 않으면 setObject를 호출하지 않는다.")
    @Test
    void queryWithNoParameter() throws SQLException {
        // given
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        PreparedStatementSetter preparedStatementSetter = (preparedStatement) -> {
        };
        RowMapper<?> mapper = mock();

        // when
        jdbcTemplate.query("select * from user", preparedStatementSetter, mapper);

        // then
        verify(preparedStatement, times(0)).setObject(anyInt(), any());
        verify(preparedStatement).close();
        verify(connection).close();
        verify(resultSet).close();
    }

    @DisplayName("단건 조회 쿼리문에 파라미터가 설정되어 있지 않으면 setObject를 호출하지 않는다.")
    @Test
    void queryForObject() throws SQLException {
        // given
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        PreparedStatementSetter preparedStatementSetter = (preparedStatement) -> {
        };
        RowMapper<?> mapper = mock();

        // when
        jdbcTemplate.query("select * from user", preparedStatementSetter, mapper);

        // then
        verify(preparedStatement, times(0)).setObject(anyInt(), any());
        verify(preparedStatement).close();
        verify(connection).close();
        verify(resultSet).close();
    }

    @DisplayName("queryForObject 내에 쿼리문 실행 중 예외가 발생할 경우 관련된 리소스들이 닫혀야 한다.")
    @Test
    void queryForObjectWithExecuteQueryException() throws SQLException {
        // given
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        PreparedStatementSetter preparedStatementSetter = (preparedStatement) -> {
            preparedStatement.setString(1, "pororo");
            preparedStatement.setString(2, "poke");
        };
        SQLException sqlException = new SQLException("조회 중 예외 발생");
        RowMapper<?> mapper = mock();
        given(this.preparedStatement.executeQuery()).willThrow(sqlException);

        // when
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> jdbcTemplate.queryForObject("select error", preparedStatementSetter, mapper))
                .withCause(sqlException);

        // then
        verify(connection).close();
        verify(preparedStatement).close();
        verifyNoInteractions(resultSet);
    }

    @DisplayName("query 내에 row mapping 도중 예외가 발생할 경우 관련된 리소스들이 닫혀야 한다.")
    @Test
    void queryForObjectWithRowMapException() throws SQLException {
        // given
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        PreparedStatementSetter preparedStatementSetter = (preparedStatement) -> {
            preparedStatement.setString(1, "pororo");
            preparedStatement.setString(2, "poke");
        };
        SQLException sqlException = new SQLException("map 중 예외 발생");
        RowMapper<?> mapper = mock();
        given(this.resultSet.next()).willThrow(sqlException);

        // when
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> jdbcTemplate.queryForObject("select error", preparedStatementSetter, mapper))
                .withCause(sqlException);

        // then
        verify(connection).close();
        verify(preparedStatement).close();
        verify(resultSet).close();
    }

    @DisplayName("queryForObject 실행 중 결과가 2개 이상 나올 경우 예외를 발생한다.")
    @Test
    void queryForObjectWithMultipleResultException() throws SQLException {
        // given
        String sql = "select account, password from user where account like 'po%' or password like 'po%''";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        PreparedStatementSetter preparedStatementSetter = (preparedStatement) -> {
        };
        given(resultSet.next()).willReturn(true, true, false);
        given(resultSet.getString("account")).willReturn("pororo");
        given(resultSet.getString("password")).willReturn("poke");

        RowMapper<TestUser> rowMapper = (resultSet) -> new TestUser(
                resultSet.getString("account"),
                resultSet.getString("password")
        );

        // when
        assertThatExceptionOfType(IncorrectResultSizeDataAccessException.class)
                .isThrownBy(() -> jdbcTemplate.queryForObject(sql, preparedStatementSetter, rowMapper));

        // then
        verify(this.resultSet).close();
        verify(this.connection).close();
        verify(this.preparedStatement).close();
    }

    @DisplayName("queryForObject 실행 중 결과가 나오지 않을 경우 예외를 발생한다.")
    @Test
    void queryForObjectWithNoResultException() throws SQLException {
        // given
        String sql = "select account, password from user where account like 'po%' or password like 'po%''";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        PreparedStatementSetter preparedStatementSetter = (preparedStatement) -> {
        };
        given(resultSet.next()).willReturn(false);

        RowMapper<TestUser> rowMapper = (resultSet) -> new TestUser(
                resultSet.getString("account"),
                resultSet.getString("password")
        );

        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> jdbcTemplate.queryForObject(sql, preparedStatementSetter, rowMapper));

        // then
        verify(this.resultSet).close();
        verify(this.connection).close();
        verify(this.preparedStatement).close();
    }

    record TestUser(String account, String password) {
    }
}
