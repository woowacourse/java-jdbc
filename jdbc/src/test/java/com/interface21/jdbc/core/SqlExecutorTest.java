package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.IncorrectParameterCountException;
import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import samples.TestUser;

class SqlExecutorTest {

    private SqlExecutor sqlExecutor;
    private DataSource dataSource;
    private PreparedStatement preparedStatement;
    private ParameterMetaData parameterMetaData;

    @BeforeEach
    void setUp() throws SQLException {
        sqlExecutor = new SqlExecutor();
        dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        parameterMetaData = mock(ParameterMetaData.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.getParameterMetaData()).thenReturn(parameterMetaData);
    }

    @DisplayName("정상적인 SQL 실행 시, 파라미터가 설정되고 쿼리가 실행된다.")
    @Test
    void execute() throws SQLException {
        // given
        TestUser user = new TestUser(1L, "mia");
        String sql = "select * from users where id = ?";
        Object[] parameters = {1};
        PreparedStatementExecutor<TestUser> statementExecutor = preparedStatement -> {
            preparedStatement.execute();
            return user;
        };
        when(parameterMetaData.getParameterCount()).thenReturn(1);

        // when
        TestUser actual = sqlExecutor.execute(sql, dataSource, statementExecutor, parameters);

        // then
        assertThat(actual).isEqualTo(user);
        verify(preparedStatement).setObject(1, 1);
        verify(preparedStatement).execute();
        verify(preparedStatement).close();
    }

    @DisplayName("SQLException 발생 시 DataAccessException으로 변환된다.")
    @Test
    void execute_SQLException_convertTo_dataAccessException() throws SQLException {
        // given
        String sql = "select * from users where id = ?";
        Object[] parameters = {1};
        PreparedStatementExecutor<Integer> statementExecutor = preparedStatement -> {
            throw new SQLException();
        };
        when(parameterMetaData.getParameterCount()).thenReturn(1);

        // when & then
        assertThatThrownBy(() -> sqlExecutor.execute(sql, dataSource, statementExecutor, parameters))
                .isInstanceOf(DataAccessException.class);
        verify(preparedStatement).setObject(1, 1);
        verify(preparedStatement).close();
    }

    @DisplayName("파라미터 개수가 일치하지 않을 경우 예외를 발생한다.")
    @Test
    void execute_throwsException_whenIncorrectParameterCount() throws SQLException {
        // given
        String sql = "update users set account = ? where id = ?";
        Object[] parameters = {1};
        PreparedStatementExecutor<Void> statementExecutor = preparedStatement -> null;
        when(parameterMetaData.getParameterCount()).thenReturn(2);

        // when & then
        assertThatThrownBy(() -> sqlExecutor.execute(sql, dataSource, statementExecutor, parameters))
                .isInstanceOf(IncorrectParameterCountException.class);
        verify(preparedStatement, never()).execute();
        verify(preparedStatement).close();
    }
}
