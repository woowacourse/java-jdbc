package com.interface21.jdbc.datasource;

import com.interface21.transaction.support.TransactionSynchronizationManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class DataSourceUtilsTest {

    private DataSource dataSource;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        given(dataSource.getConnection()).willReturn(connection);
    }

    @Test
    void 트랜잭션이_없으면_새_Connection을_가져오고_반납할_때_닫는다() throws SQLException {
        final Connection actual = DataSourceUtils.getConnection(dataSource);
        DataSourceUtils.releaseConnection(actual, dataSource);

        assertThat(actual).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        verify(connection).close();
    }

    @Test
    void 트랜잭션에_바인딩된_Connection을_가져오고_반납해도_닫지_않는다() throws SQLException {
        final Connection txConnection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, txConnection);

        try {
            final Connection actual = DataSourceUtils.getConnection(dataSource);
            DataSourceUtils.releaseConnection(actual, dataSource);

            assertThat(actual).isSameAs(txConnection);
            verify(dataSource, never()).getConnection();
            verify(txConnection, never()).close();
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }
}
