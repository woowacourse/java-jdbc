package com.interface21.transaction.support;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.transaction.annotation.Transactional;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TransactionalProxyTest {

    private JdbcConnectionPool dataSource;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        dataSource = JdbcConnectionPool.create("jdbc:h2:mem:" + UUID.randomUUID(), "", "");
        dataSource.setMaxConnections(1);
        dataSource.setLoginTimeout(1);
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update("create table entries (id int primary key)");
    }

    @AfterEach
    void cleanup() {
        try {
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
            assertThat(dataSource.getActiveConnections()).isZero();
        } finally {
            dataSource.dispose();
        }
    }

    @Test
    void annotatedMethodCommitsAndReturnsResult() throws Exception {
        final var service = proxy(() -> {
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNotNull();
            jdbcTemplate.update("insert into entries values (?)", 1);
        });

        assertThat(service.transactional()).isEqualTo("completed");
        assertCount(1L);
    }

    @Test
    void unannotatedMethodDoesNotStartTransaction() throws Exception {
        final var service = proxy(() -> {
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
            jdbcTemplate.update("insert into entries values (?)", 1);
        });

        service.plain();
        assertCount(1L);
    }

    @Test
    void checkedExceptionRollsBackAndPreservesOriginalException() {
        final Exception failure = new Exception("business failed");
        final var service = proxy(() -> {
            jdbcTemplate.update("insert into entries values (?)", 1);
            throw failure;
        });

        assertThatThrownBy(service::transactional).isSameAs(failure);
        assertCount(0L);
    }

    @Test
    void errorRollsBackAndPreservesOriginalError() {
        final AssertionError failure = new AssertionError("business failed");
        final var service = proxy(() -> {
            jdbcTemplate.update("insert into entries values (?)", 1);
            throw failure;
        });

        assertThatThrownBy(service::transactional).isSameAs(failure);
        assertCount(0L);
    }

    @Test
    void connectionIsNotSharedWithAnotherThread() throws Exception {
        final var service = proxy(() -> {
            final var connection = TransactionSynchronizationManager.getResource(dataSource);
            assertThat(connection).isNotNull();
            try (var executor = Executors.newSingleThreadExecutor()) {
                assertThat(executor.submit(() -> TransactionSynchronizationManager.getResource(dataSource)).get())
                        .isNull();
            }
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        });

        service.transactional();
    }

    private TestService proxy(final Action action) {
        return TransactionalProxy.create(TestService.class, new TestServiceImpl(action), dataSource);
    }

    private void assertCount(final long expected) {
        final Long count = jdbcTemplate.queryForObject("select count(*) from entries", (rs, rowNum) -> rs.getLong(1));
        assertThat(count).isEqualTo(expected);
    }

    public interface TestService {
        @Transactional
        String transactional() throws Exception;
        void plain() throws Exception;
    }

    @FunctionalInterface
    public interface Action {
        void run() throws Exception;
    }

    public static class TestServiceImpl implements TestService {
        private final Action action;

        public TestServiceImpl(final Action action) {
            this.action = action;
        }

        @Override
        public String transactional() throws Exception {
            action.run();
            return "completed";
        }

        @Override
        public void plain() throws Exception {
            action.run();
        }
    }
}
