package transaction.stage1;

import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.utility.DockerImageName;
import transaction.DatabasePopulatorUtils;
import transaction.RunnableWrapper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class Stage1Test {

    private static final Logger log = LoggerFactory.getLogger(Stage1Test.class);
    private DataSource dataSource;
    private UserDao userDao;

    private void setUp(final DataSource dataSource) {
        this.dataSource = dataSource;
        DatabasePopulatorUtils.execute(dataSource);
        this.userDao = new UserDao(dataSource);
    }

    @Test
    void dirtyReading() throws SQLException {
        setUp(createH2DataSource());

        final var connection = dataSource.getConnection();
        connection.setAutoCommit(false);

        TransactionSynchronizationManager.bindResource(dataSource, connection);
        userDao.insert(new User("gugu", "password", "hkkang@woowahan.com"));
        TransactionSynchronizationManager.unbindResource(dataSource);

        new Thread(RunnableWrapper.accept(() -> {
            final var subConnection = dataSource.getConnection();
            final int isolationLevel = Connection.TRANSACTION_READ_UNCOMMITTED;
            subConnection.setTransactionIsolation(isolationLevel);

            TransactionSynchronizationManager.bindResource(dataSource, subConnection);
            final var actual = userDao.findByAccount("gugu");
            TransactionSynchronizationManager.unbindResource(dataSource);

            log.info("isolation level : {}, user : {}", isolationLevel, actual);
            assertThat(actual).isNotNull();
        })).start();

        sleep(0.5);

        connection.rollback();
    }

    @Test
    void noneRepeatable() throws SQLException {
        setUp(createH2DataSource());

        try (final var connection = dataSource.getConnection()) {
            userDao.insert(new User("gugu", "password", "hkkang@woowahan.com"));
        }

        final var connection = dataSource.getConnection();
        connection.setAutoCommit(false);
        final int isolationLevel = Connection.TRANSACTION_READ_COMMITTED;
        connection.setTransactionIsolation(isolationLevel);

        TransactionSynchronizationManager.bindResource(dataSource, connection);
        final var user = userDao.findByAccount("gugu");
        log.info("user : {}", user);

        new Thread(RunnableWrapper.accept(() -> {
            try (final var subConnection = dataSource.getConnection()) {
                final var anotherUser = userDao.findByAccount("gugu");
                anotherUser.changePassword("qqqq");
                userDao.update(anotherUser);
            }
        })).start();

        sleep(0.5);

        final var actual = userDao.findByAccount("gugu");
        TransactionSynchronizationManager.unbindResource(dataSource);

        log.info("isolation level : {}, user : {}", isolationLevel, actual);
        assertThat(actual.getPassword()).isNotEqualTo("password");

        connection.rollback();
    }

    @Test
    void phantomReading() throws SQLException {
        final var mysql = new MySQLContainer<>(DockerImageName.parse("mysql:8.0.30"))
                .withLogConsumer(new Slf4jLogConsumer(log));
        mysql.start();
        setUp(createMySQLDataSource(mysql));

        try (final var connection = dataSource.getConnection()) {
            userDao.insert(new User("gugu", "password", "hkkang@woowahan.com"));
        }

        final var connection = dataSource.getConnection();
        connection.setAutoCommit(false);
        final int isolationLevel = Connection.TRANSACTION_REPEATABLE_READ;
        connection.setTransactionIsolation(isolationLevel);

        TransactionSynchronizationManager.bindResource(dataSource, connection);
        userDao.findGreaterThan(1L);

        new Thread(RunnableWrapper.accept(() -> {
            try (final var subConnection = dataSource.getConnection()) {
                subConnection.setAutoCommit(false);
                userDao.insert(new User("bird", "password", "bird@woowahan.com"));
                subConnection.commit();
            }
        })).start();

        sleep(0.5);

        userDao.updatePasswordGreaterThan("qqqq", 1L);

        final var actual = userDao.findGreaterThan(1L);
        TransactionSynchronizationManager.unbindResource(dataSource);

        log.info("isolation level : {}, user : {}", isolationLevel, actual);
        assertThat(actual).hasSize(1);

        connection.rollback();
        mysql.close();
    }

    private static DataSource createMySQLDataSource(final JdbcDatabaseContainer<?> container) {
        final var config = new HikariConfig();
        config.setJdbcUrl(container.getJdbcUrl());
        config.setUsername(container.getUsername());
        config.setPassword(container.getPassword());
        config.setDriverClassName(container.getDriverClassName());
        return new HikariDataSource(config);
    }

    private static DataSource createH2DataSource() {
        final var jdbcDataSource = new JdbcDataSource();
        jdbcDataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=MYSQL;");
        jdbcDataSource.setUser("sa");
        jdbcDataSource.setPassword("");
        return jdbcDataSource;
    }

    private void sleep(double seconds) {
        try {
            TimeUnit.MILLISECONDS.sleep((long) (seconds * 1000));
        } catch (InterruptedException ignored) {
        }
    }
}
