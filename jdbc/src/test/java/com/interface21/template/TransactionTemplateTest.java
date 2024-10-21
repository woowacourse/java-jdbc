package com.interface21.template;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.TransactionCallBack;
import com.interface21.jdbc.datasource.DataSourceUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class TransactionTemplateTest {

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
    @DisplayName("예외가 발생되지 않는 경우 정상적으로 작동 후 commit 한다.")
    void doCallBackTransaction_WhenSuccess() throws SQLException {
        when(DataSourceUtils.getConnection(dataSource))
                .thenReturn(connection);

        TransactionTemplate transactionTemplate = new TransactionTemplate(dataSource);
        transactionTemplate.doTransaction(transactionCallBack);

        verify(connection).setAutoCommit(false);
        verify(transactionCallBack).doExecute();
        verify(connection).commit();
        verify(connection, never()).rollback();
    }

    @Test
    @DisplayName("예외가 발생되지 않는 경우 정상적으로 작동 후 commit 한다.")
    void doRunnableTransaction_WhenSuccess() throws SQLException {
        when(DataSourceUtils.getConnection(dataSource))
                .thenReturn(connection);

        TransactionTemplate transactionTemplate = new TransactionTemplate(dataSource);
        transactionTemplate.doTransaction(runnable);

        verify(connection).setAutoCommit(false);
        verify(runnable).run();
        verify(connection).commit();
        verify(connection, never()).rollback();
    }

    @Test
    @DisplayName("예외가 발생되는 경우 commit 하지 않고 rollback 한다.")
    void doCallBackTransaction_WhenException() throws SQLException {
        doThrow(new DataAccessException("exception 발생"))
                .when(transactionCallBack).doExecute();
        when(DataSourceUtils.getConnection(dataSource))
                .thenReturn(connection);

        TransactionTemplate transactionTemplate = new TransactionTemplate(dataSource);

        assertThrows(DataAccessException.class, () ->
                transactionTemplate.doTransaction(transactionCallBack)
        );

        verify(connection, never()).commit();
        verify(connection, atLeastOnce()).rollback();
    }

    @Test
    @DisplayName("예외가 발생되는 경우 commit 하지 않고 rollback 한다.")
    void doRunnableTransaction_WhenException() throws SQLException {
        doThrow(new DataAccessException("exception 발생"))
                .when(runnable).run();
        when(DataSourceUtils.getConnection(dataSource))
                .thenReturn(connection);

        TransactionTemplate transactionTemplate = new TransactionTemplate(dataSource);

        assertThrows(DataAccessException.class, () ->
                transactionTemplate.doTransaction(runnable)
        );

        verify(connection, never()).commit();
        verify(connection, atLeastOnce()).rollback();
    }
}
