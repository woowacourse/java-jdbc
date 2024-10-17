package com.interface21.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class TransactionSynchronizationManagerTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    @DisplayName("Connection bind")
    @Test
    void bindResource() {
        TransactionSynchronizationManager.bindResource(dataSource,connection);

        Connection actual = TransactionSynchronizationManager.getResource(dataSource);

        assertThat(actual).isEqualTo(connection);
    }

    @DisplayName("Connection unbind")
    @Test
    void unbindResource() {
        TransactionSynchronizationManager.bindResource(dataSource,connection);

        Connection actual = TransactionSynchronizationManager.unbindResource(dataSource);

        assertThat(actual).isEqualTo(connection);
    }
}
