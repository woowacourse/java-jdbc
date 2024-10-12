package com.interface21.jdbc.core;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SqlExecutorTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final PreparedStatement preparedStatement = mock(PreparedStatement.class);

    private SqlExecutor sqlExecutor;

    @BeforeEach
    void setUp() throws SQLException {
        this.sqlExecutor = new SqlExecutor(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
    }

    @Test
    @DisplayName("sql을 실행한다.")
    void execute() throws SQLException {
        // given
        String sql = "select * from users";
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);

        PreparedStatementExecutor<Void> executor = mock(PreparedStatementExecutor.class);

        // when
        sqlExecutor.execute(sql, executor);

        // then
        verify(executor).execute(preparedStatement);
    }
}
