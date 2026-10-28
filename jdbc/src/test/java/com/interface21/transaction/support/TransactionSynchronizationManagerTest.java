package com.interface21.transaction.support;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class TransactionSynchronizationManagerTest {

    private DataSource dataSource;
    private DataSource otherDataSource;
    private Connection connection;

    @BeforeEach
    void setUp() {
        dataSource = mock(DataSource.class);
        otherDataSource = mock(DataSource.class);
        connection = mock(Connection.class);
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.unbindResource(dataSource);
        TransactionSynchronizationManager.unbindResource(otherDataSource);
    }

    @Test
    void 등록되지_않은_데이터소스를_조회하면_null을_반환한다() {
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void 등록한_커넥션을_그대로_조회한다() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }

    @Test
    void 데이터소스별로_서로_다른_커넥션을_보관한다() {
        final var otherConnection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        TransactionSynchronizationManager.bindResource(otherDataSource, otherConnection);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(otherDataSource)).isSameAs(otherConnection);
    }

    @Test
    void 커넥션을_해제하면_해당_등록만_제거하고_커넥션을_닫지_않는다() throws SQLException {
        final var otherConnection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        TransactionSynchronizationManager.bindResource(otherDataSource, otherConnection);

        final var unboundConnection = TransactionSynchronizationManager.unbindResource(dataSource);

        assertThat(unboundConnection).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        assertThat(TransactionSynchronizationManager.getResource(otherDataSource)).isSameAs(otherConnection);
        verify(connection, never()).close();
    }

    @Test
    void 등록되지_않은_데이터소스를_해제하면_null을_반환한다() {
        assertThat(TransactionSynchronizationManager.unbindResource(dataSource)).isNull();
    }

    @Test
    void 같은_데이터소스에_중복_등록하면_기존_커넥션을_유지하고_예외를_던진다() {
        final var otherConnection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(dataSource, otherConnection))
                .isInstanceOf(IllegalStateException.class);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }

    @Test
    void 같은_데이터소스라도_스레드별로_커넥션을_분리한다() throws Exception {
        final var otherConnection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        try (final var executor = Executors.newSingleThreadExecutor()) {
            final var task = executor.submit(() -> {
                assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
                try {
                    TransactionSynchronizationManager.bindResource(dataSource, otherConnection);
                    assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(otherConnection);
                } finally {
                    TransactionSynchronizationManager.unbindResource(dataSource);
                }
                assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
            });

            task.get(5, TimeUnit.SECONDS);
        }

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }
}
