package com.interface21.jdbc.datasource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DataSourceUtilsTest {

    private DataSource dataSource;
    private Connection connectionFromDataSource;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connectionFromDataSource = mock(Connection.class);

        when(dataSource.getConnection()).thenReturn(connectionFromDataSource);
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.findConnection(dataSource)
                .ifPresent(ignored ->
                        TransactionSynchronizationManager.unbindResource(dataSource));
    }

    @Test
    void 바인딩된_커넥션이_없으면_getConnection_시_새_커넥션을_얻어_바인딩한다() throws SQLException {
        //when
        DataSourceUtils.getConnection(dataSource);

        //then
        assertThat(TransactionSynchronizationManager.findConnection(dataSource)).contains(connectionFromDataSource);
        verify(dataSource).getConnection();
    }

    @Test
    void 바인딩된_커넥션이_있으면_getConnection_시_새로_얻지_않고_재사용한다() throws SQLException {
        //given
        Connection bound = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, bound);

        //when
        Connection actual = DataSourceUtils.getConnection(dataSource);

        //then
        assertThat(actual).isSameAs(bound);
        verify(dataSource, never()).getConnection();
    }

    @Test
    void 커넥션을_얻지_못하면_getConnection_시_예외가_발생하고_바인딩하지_않는다() throws SQLException {
        //given
        when(dataSource.getConnection()).thenThrow(new SQLException());

        //when & then
        assertThatThrownBy(() -> DataSourceUtils.getConnection(dataSource))
                .isInstanceOf(CannotGetJdbcConnectionException.class);
        assertThat(TransactionSynchronizationManager.findConnection(dataSource)).isEmpty();
    }

    @Test
    void releaseConnection_시_바인딩을_해제하고_커넥션을_닫는다() throws SQLException {
        //given
        TransactionSynchronizationManager.bindResource(dataSource, connectionFromDataSource);

        //when
        DataSourceUtils.releaseConnection(connectionFromDataSource, dataSource);

        //then
        assertThat(TransactionSynchronizationManager.findConnection(dataSource)).isEmpty();
        verify(connectionFromDataSource).close();
    }

    @Test
    void 커넥션을_닫지_못하면_releaseConnection_시_예외가_발생하지만_바인딩은_해제된다() throws SQLException {
        //given
        TransactionSynchronizationManager.bindResource(dataSource, connectionFromDataSource);
        doThrow(new SQLException()).when(connectionFromDataSource).close();

        //when & then
        assertThatThrownBy(() -> DataSourceUtils.releaseConnection(connectionFromDataSource, dataSource))
                .isInstanceOf(DataAccessException.class);
        assertThat(TransactionSynchronizationManager.findConnection(dataSource)).isEmpty();
    }
}
