package transaction.stage3;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionSynchronizationTest {

    @Test
    void sameConnectionIsReusedAndDaoWritesRollBackTogether() {
        final var source = new JdbcDataSource();
        source.setURL("jdbc:h2:mem:synchronization-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        final var jdbc = new JdbcTemplate(source);
        jdbc.execute("create table events (name varchar(100))");
        final var transaction = new TransactionTemplate(new DataSourceTransactionManager(source));

        try {
            transaction.executeWithoutResult(status -> {
                assertThat(TransactionSynchronizationManager.hasResource(source)).isTrue();
                final var first = DataSourceUtils.getConnection(source);
                final var second = DataSourceUtils.getConnection(source);
                assertThat(second).isSameAs(first);

                // DAO를 대신하는 두 SQL 호출에 Connection을 전달하지 않는다.
                jdbc.update("insert into events values (?)", "password changed");
                DataSourceUtils.releaseConnection(first, source);
                DataSourceUtils.releaseConnection(second, source);
                try {
                    assertThat(first.isClosed()).isFalse();
                } catch (SQLException e) {
                    throw new AssertionError(e);
                }
                jdbc.update("insert into events values (?)", "history saved");
                assertThat(jdbc.queryForObject("select count(*) from events", Integer.class)).isEqualTo(2);
                status.setRollbackOnly();
            });

            assertThat(TransactionSynchronizationManager.hasResource(source)).isFalse();
            assertThat(jdbc.queryForObject("select count(*) from events", Integer.class)).isZero();
        } finally {
            jdbc.execute("shutdown");
        }
    }

    @Test
    void boundResourceIsNotInheritedByAnotherThread() throws Exception {
        final var key = new Object();
        final var resource = new Object();
        TransactionSynchronizationManager.bindResource(key, resource);
        try (final var executor = Executors.newSingleThreadExecutor()) {
            assertThat(TransactionSynchronizationManager.getResource(key)).isSameAs(resource);
            final var otherThreadResource = executor.submit(
                    () -> TransactionSynchronizationManager.getResource(key)).get(5, TimeUnit.SECONDS);
            assertThat(otherThreadResource).isNull();
        } finally {
            TransactionSynchronizationManager.unbindResource(key);
        }
        assertThat(TransactionSynchronizationManager.hasResource(key)).isFalse();
    }
}
