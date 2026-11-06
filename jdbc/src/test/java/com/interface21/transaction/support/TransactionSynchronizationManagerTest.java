package com.interface21.transaction.support;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    void 바인딩을_해제한다() {
        TransactionSynchronizationManager.unbindResource(dataSource);
    }

    @Nested
    class GetResource {

        @Test
        void 바인딩한_커넥션을_조회한다() {
            TransactionSynchronizationManager.bindResource(dataSource, connection);

            final Connection actual = TransactionSynchronizationManager.getResource(dataSource);

            assertThat(actual).isSameAs(connection);
        }

        @Test
        void 바인딩된_커넥션이_없으면_null을_반환한다() {
            final Connection actual = TransactionSynchronizationManager.getResource(dataSource);

            assertThat(actual).isNull();
        }

        @Test
        void 다른_스레드에서는_바인딩한_커넥션이_조회되지_않는다() {
            TransactionSynchronizationManager.bindResource(dataSource, connection);

            final Connection actual = CompletableFuture
                    .supplyAsync(() -> TransactionSynchronizationManager.getResource(dataSource))
                    .join();

            assertThat(actual).isNull();
        }
    }

    @Nested
    class BindResource {

        @Test
        void 이미_바인딩된_DataSource에_다시_바인딩하면_예외를_던진다() {
            TransactionSynchronizationManager.bindResource(dataSource, connection);

            assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(dataSource, mock(Connection.class)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("이미 Connection이 바인딩된 DataSource입니다.");
        }

        @Test
        void DataSource별로_다른_커넥션을_바인딩한다() {
            final DataSource otherDataSource = mock(DataSource.class);
            final Connection otherConnection = mock(Connection.class);
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            TransactionSynchronizationManager.bindResource(otherDataSource, otherConnection);

            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
            assertThat(TransactionSynchronizationManager.getResource(otherDataSource)).isSameAs(otherConnection);

            TransactionSynchronizationManager.unbindResource(otherDataSource);
        }
    }

    @Nested
    class UnbindResource {

        @Test
        void 바인딩을_해제하면_해제한_커넥션을_반환하고_더_이상_조회되지_않는다() {
            TransactionSynchronizationManager.bindResource(dataSource, connection);

            final Connection actual = TransactionSynchronizationManager.unbindResource(dataSource);

            assertThat(actual).isSameAs(connection);
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        }

        @Test
        void 바인딩된_커넥션이_없으면_null을_반환한다() {
            final Connection actual = TransactionSynchronizationManager.unbindResource(dataSource);

            assertThat(actual).isNull();
        }
    }
}
