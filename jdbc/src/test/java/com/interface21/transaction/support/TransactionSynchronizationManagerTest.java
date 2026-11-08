package com.interface21.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.sql.Connection;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        TransactionSynchronizationManager.findConnection(dataSource)
                .ifPresent(ignored ->
                        TransactionSynchronizationManager.unbindResource(dataSource));
    }

    @Test
    void 바인딩된_커넥션이_없으면_findConnection_시_빈_값을_반환한다() {
        assertThat(TransactionSynchronizationManager.findConnection(dataSource)).isEmpty();
    }

    @Test
    void 커넥션을_바인딩하면_findConnection_시_바인딩한_커넥션을_반환한다() {
        //given
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        //when & then
        assertThat(TransactionSynchronizationManager.findConnection(dataSource)).contains(connection);
    }

    @Test
    void 다른_DataSource로_바인딩된_커넥션은_findConnection_시_조회되지_않는다() {
        //given
        DataSource otherDataSource = mock(DataSource.class);
        TransactionSynchronizationManager.bindResource(otherDataSource, connection);

        //when & then
        try {
            assertThat(TransactionSynchronizationManager.findConnection(dataSource)).isEmpty();
        } finally {
            TransactionSynchronizationManager.unbindResource(otherDataSource);
        }
    }

    @Test
    void 커넥션을_언바인딩하면_바인딩했던_커넥션을_반환하고_이후_조회되지_않는다() {
        //given
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        //when
        Connection unbound = TransactionSynchronizationManager.unbindResource(dataSource);

        //then
        assertThat(unbound).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.findConnection(dataSource)).isEmpty();
    }

    @Test
    void 바인딩된_커넥션이_없으면_unbindResource_시_예외가_발생한다() {
        assertThatThrownBy(() -> TransactionSynchronizationManager.unbindResource(dataSource))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 다른_DataSource만_바인딩되어_있으면_unbindResource_시_예외가_발생한다() {
        //given
        DataSource otherDataSource = mock(DataSource.class);
        TransactionSynchronizationManager.bindResource(otherDataSource, connection);

        //when & then
        try {
            assertThatThrownBy(() -> TransactionSynchronizationManager.unbindResource(dataSource))
                    .isInstanceOf(IllegalStateException.class);
        } finally {
            TransactionSynchronizationManager.unbindResource(otherDataSource);
        }
    }

    @Test
    void 이미_언바인딩한_커넥션을_다시_unbindResource_하면_예외가_발생한다() {
        //given
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        TransactionSynchronizationManager.unbindResource(dataSource);

        //when & then
        assertThatThrownBy(() -> TransactionSynchronizationManager.unbindResource(dataSource))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 다른_스레드에서_바인딩한_커넥션은_findConnection_시_조회되지_않는다() throws InterruptedException {
        //given
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        //when
        AtomicReference<Optional<Connection>> found = new AtomicReference<>();

        Thread thread = new Thread(() -> found.set(TransactionSynchronizationManager.findConnection(dataSource)));
        thread.start();
        thread.join();

        //then
        assertThat(found.get()).isEmpty();
    }
}
