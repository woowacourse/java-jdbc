package org.springframework.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class TransactionSynchronizationManagerTest {

    @Mock
    private DataSource dataSource;
    @Mock
    private Connection connection;

    @Test
    void getResource() {
        // when
        Connection actual = TransactionSynchronizationManager.getResource(dataSource);

        // then
        assertThat(actual).isNull();
    }

    @Test
    void bindResource() {
        // when
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        // then
        Connection actual = TransactionSynchronizationManager.getResource(dataSource);
        assertThat(actual).isEqualTo(connection);
    }

    @Test
    void unbindResource() {
        // given
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        // when
        Connection unboundConnection = TransactionSynchronizationManager.unbindResource(dataSource);

        // then
        Connection actual = TransactionSynchronizationManager.getResource(dataSource);

        assertThat(unboundConnection).isEqualTo(connection);
        assertThat(actual).isNull();
    }
}
