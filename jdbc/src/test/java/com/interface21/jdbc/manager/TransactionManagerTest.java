package com.interface21.jdbc.manager;

import com.interface21.jdbc.core.TransactionCallBack;
import com.interface21.jdbc.datasource.DataSourceUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.mockito.Mockito.*;

class TransactionManagerTest {

    private Connection connection;
    private DataSource dataSource;
    private TransactionCallBack<?> transactionCallBack;
    private Runnable runnable;

    @BeforeEach
    void setUp() {
        this.connection = mock(Connection.class);
        this.dataSource = mock(DataSource.class);
        this.transactionCallBack = mock(TransactionCallBack.class);
        this.runnable = mock(Runnable.class);
    }

    @Test
    @DisplayName("DoBegin을 하는 경우 auto commit을 false로 변경한다.")
    void doBegin() throws SQLException {
        when(DataSourceUtils.getConnection(dataSource))
                .thenReturn(connection);

        TransactionManager transactionManager = new TransactionManager(dataSource);
        transactionManager.doBegin(dataSource);

        verify(connection).setAutoCommit(false);
    }

    @Test
    @DisplayName("doCommit을 하는 경우 connection을 commit 한다.")
    void doCommit() throws SQLException {
        when(DataSourceUtils.getConnection(dataSource))
                .thenReturn(connection);

        TransactionManager transactionManager = new TransactionManager(dataSource);
        transactionManager.doCommit(dataSource);

        verify(connection).commit();
    }

    @Test
    @DisplayName("doRollBack 하는 경우 connection을 rollback 한다.")
    void doRollBack() throws SQLException {
        when(DataSourceUtils.getConnection(dataSource))
                .thenReturn(connection);

        TransactionManager transactionManager = new TransactionManager(dataSource);
        transactionManager.doRollback(dataSource);

        verify(connection).rollback();
    }

    @Test
    @DisplayName("doClose 하는 경우 connection을 close 한다.")
    void doClose() throws SQLException {
        when(DataSourceUtils.getConnection(dataSource))
                .thenReturn(connection);

        TransactionManager transactionManager = new TransactionManager(dataSource);
        transactionManager.doClose(dataSource);

        verify(connection).close();
    }
}
