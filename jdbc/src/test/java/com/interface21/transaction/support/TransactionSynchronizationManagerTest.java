package com.interface21.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TransactionSynchronizationManagerTest {

    private DataSource dataSource;
    private Connection connection;

    @BeforeEach
    void setUp() {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
    }

    @DisplayName("Connection을 bind한다.")
    @Test
    void getResource() {
        // given
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        // when
        Connection returedConnection = TransactionSynchronizationManager.getResource(dataSource);

        // then
        assertThat(returedConnection).isEqualTo(connection);
    }

    @DisplayName("Connection을 unbind한다.")
    @Test
    void bindResource() {
        // given
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        // when
        Connection removedConnection = TransactionSynchronizationManager.unbindResource(dataSource);

        // then
        assertAll(
                () -> assertThat(removedConnection).isEqualTo(connection),
                () -> assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull()
        );
    }

    @DisplayName("이미  unbind된 Connection을 unbind시, null을 반환한다.")
    @Test
     void bindResource_When_NotBound() {
        // given
        Connection result = TransactionSynchronizationManager.unbindResource(dataSource);

        // when && then
        assertThat(result).isNull();
    }
}
