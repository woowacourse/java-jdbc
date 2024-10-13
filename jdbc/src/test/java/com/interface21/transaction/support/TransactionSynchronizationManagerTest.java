package com.interface21.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TransactionSynchronizationManagerTest {

    @Test
    @DisplayName("Connection 객체를 보관하고 가져온다.")
    void bindAndGetResource() {
        // given
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);

        // when
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        Connection retrievedConnection = TransactionSynchronizationManager.getResource(dataSource);

        // then
        assertThat(retrievedConnection).isSameAs(connection);
    }

    @Test
    @DisplayName("Connection 객체를 해제한다.")
    void unbindResource() {
        // given
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);

        TransactionSynchronizationManager.bindResource(dataSource, connection);

        // when
        Connection unboundConnection = TransactionSynchronizationManager.unbindResource(dataSource);

        // then
        assertThat(unboundConnection).isSameAs(connection);

        Connection retrievedConnection = TransactionSynchronizationManager.getResource(dataSource);
        assertThat(retrievedConnection).isNull();
    }
}
