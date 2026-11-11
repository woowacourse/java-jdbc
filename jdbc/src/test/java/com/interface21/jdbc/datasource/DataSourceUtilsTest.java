package com.interface21.jdbc.datasource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class DataSourceUtilsTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.unbindResource(dataSource);
    }

    @Test
    void 트랜잭션에_바인딩된_커넥션이_있으면_그_커넥션을_반환한다() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        final var actual = DataSourceUtils.getConnection(dataSource);

        assertThat(actual).isSameAs(connection);
        verifyNoInteractions(dataSource);
    }

    @Test
    void 바인딩된_커넥션이_없으면_새_커넥션을_반환하고_바인딩하지_않는다() throws SQLException {
        given(dataSource.getConnection()).willReturn(connection);

        final var actual = DataSourceUtils.getConnection(dataSource);

        assertThat(actual).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void 트랜잭션에_바인딩된_커넥션은_release해도_닫지_않는다() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        DataSourceUtils.releaseConnection(connection, dataSource);

        verify(connection, never()).close();
    }

    @Test
    void 바인딩되지_않은_커넥션은_release하면_닫는다() throws SQLException {
        DataSourceUtils.releaseConnection(connection, dataSource);

        verify(connection).close();
    }
}
