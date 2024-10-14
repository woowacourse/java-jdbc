package com.interface21.transaction.support;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class TransactionSynchronizationManagerTest {

    DataSource dataSource = mock(DataSource.class);
    Connection connection = mock(Connection.class);

    @DisplayName("데이터소스에 맞는 커넥션이 있다면 반환한다.")
    @Test
    void getResource() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        Connection bindedConnection = TransactionSynchronizationManager.getResource(dataSource);

        assertThat(bindedConnection).isNotNull();
    }

    @DisplayName("데이터소스에 맞는 커넥션이 없다면 null을 반환한다.")
    @Test
    void getResource_without_connection() {
        Connection emptyConnection = TransactionSynchronizationManager.getResource(dataSource);

        assertThat(emptyConnection).isNull();
    }

    @DisplayName("커넥션을 해제한다.")
    @Test
    void unbindResource() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        Connection bindedConnection = TransactionSynchronizationManager.getResource(dataSource);

        assertThat(bindedConnection).isNotNull();

        TransactionSynchronizationManager.unbindResource(dataSource);

        Connection unboundedConnection = TransactionSynchronizationManager.getResource(dataSource);

        assertThat(unboundedConnection).isNull();
    }
}
