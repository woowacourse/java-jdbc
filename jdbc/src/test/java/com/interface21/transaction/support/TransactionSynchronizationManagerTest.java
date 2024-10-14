package com.interface21.transaction.support;

import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;

class TransactionSynchronizationManagerTest {

    private DataSource dataSource;
    private Connection connection;

    @BeforeEach
    void setUp() {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.unbindResource(dataSource);
    }

    @DisplayName("특정 Datasource와 Connection을 바인딩한다.")
    @Test
    void bindResource() {
        // when
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        // then
        Connection result = TransactionSynchronizationManager.getResource(dataSource);
        assertThat(result).isEqualTo(connection);
    }

    @DisplayName("바인딩된 Connection이 없으면 null을 반환한다.")
    @Test
    void cannotGetResource() {
        // when
        Connection result = TransactionSynchronizationManager.getResource(dataSource);

        // then
        assertThat(result).isNull();
    }

    @DisplayName("특정 DataSource와 연결된 Connection 바인딩을 해제한다.")
    @Test
    void unbindResource() {
        // given
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        // when
        Connection result = TransactionSynchronizationManager.unbindResource(dataSource);

        // then
        assertAll(
                () -> assertThat(result).isEqualTo(connection),
                () -> assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull()
        );
    }

    @DisplayName("바인딩되지 않은 자원을 언바인딩하면 null을 반환한다.")
    @Test
    void unbindUnknownResource() {
        // when
        Connection result = TransactionSynchronizationManager.unbindResource(dataSource);

        //then
        assertThat(result).isNull();
    }
}
