package com.interface21.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class TransactionSynchronizationManagerTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.unbindResource(dataSource);
    }
    
    @DisplayName("자원을 바인딩하고 바인딩한 Connection을 가져온다.")
    @Test
    void bindResource_andThen_getResource() {
        // given
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        // when
        Connection actual = TransactionSynchronizationManager.getResource(dataSource);

        // then
        assertThat(actual).isSameAs(connection);
    }

    @DisplayName("바인딩한 Connection이 없으면 null을 리턴한다.")
    @Test
    void getResource_returnNull_whenNotBound() {
        // when
        Connection actual = TransactionSynchronizationManager.getResource(dataSource);

        // then
        assertThat(actual).isNull();
    }

    @DisplayName("자원을 언바인딩한다.")
    @Test
    void unbindResource() {
        // given
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        // when
        Connection unboundConnection = TransactionSynchronizationManager.unbindResource(dataSource);
        Connection actual = TransactionSynchronizationManager.getResource(dataSource);

        // then
        assertThat(unboundConnection).isSameAs(connection);
        assertThat(actual).isNull();
    }

    @DisplayName("바인딩이 되지 않은 자원을 언바인딩하면 null을 리턴한다.")
    @Test
    void unbindResource_returnNull_whenNotBound() {
        // when
        Connection actual = TransactionSynchronizationManager.unbindResource(dataSource);

        // then
        assertThat(actual).isNull();
    }
}
