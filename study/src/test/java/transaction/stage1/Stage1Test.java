package transaction.stage1;

import static org.assertj.core.api.Assertions.assertThat;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.utility.DockerImageName;
import transaction.DatabasePopulatorUtils;
import transaction.RunnableWrapper;

/**
 * 격리 레벨(Isolation Level)에 따라 여러 사용자가 동시에 db에 접근했을 때 어떤 문제가 발생하는지 확인해보자. ❗phantom reads는 docker를 실행한 상태에서 테스트를 실행한다.
 * ❗phantom reads는 MySQL로 확인한다. H2 데이터베이스에서는 발생하지 않는다.
 * <p>
 * 참고 링크 https://en.wikipedia.org/wiki/Isolation_(database_systems)
 * <p>
 * 각 테스트에서 어떤 현상이 발생하는지 직접 경험해보고 아래 표를 채워보자.
 * + : 발생
 * - : 발생하지 않음
 *   Read phenomena | Dirty reads | Non-repeatable reads | Phantom reads
 * Isolation level  |             |                      |
 * -----------------|-------------|----------------------|--------------
 * Read Uncommitted |      +      |           +          |       +
 * Read Committed   |      -      |           +          |       +
 * Repeatable Read  |      -      |           -          |       +
 * Serializable     |      -      |           -          |       -
 */
class Stage1Test {

    private static final Logger log = LoggerFactory.getLogger(Stage1Test.class);
    private DataSource dataSource;
    private UserDao userDao;

    private void setUp(final DataSource dataSource) {
        this.dataSource = dataSource;
        DatabasePopulatorUtils.execute(dataSource);
        this.userDao = new UserDao(dataSource);
    }

    /**
     * 격리 수준에 따라 어떤 현상이 발생하는지 테스트를 돌려 직접 눈으로 확인하고 표를 채워보자.
     * + : 발생
     * - : 발생하지 않음
     *   Read phenomena | Dirty reads
     * Isolation level  |
     * -----------------|-------------
     * Read Uncommitted | +
     * Read Committed   | -
     * Repeatable Read  | -
     * Serializable     | -
     */
    @ParameterizedTest
    @MethodSource("isDirtyReadOccurred")
    @DisplayName("트랜잭션 격리 수준에 따른 Dirty Reads 테스트")
    void dirtyReading(int isolationLevel, boolean isDirtyReadOccurred) throws SQLException {
        setUp(createH2DataSource());
        Connection connection = dataSource.getConnection();

        connection.setAutoCommit(false);
        userDao.insert(connection, new User("gugu", "password", "hkkang@woowahan.com"));

        new Thread(RunnableWrapper.accept(() -> {
            Connection subConnection = dataSource.getConnection();
            subConnection.setTransactionIsolation(isolationLevel);
            User actual = userDao.findByAccount(subConnection, "gugu");

            assertThat(actual != null).isEqualTo(isDirtyReadOccurred);
        })).start();

        sleep(0.5);
        connection.rollback();
    }

    static Stream<Arguments> isDirtyReadOccurred() {
        return Stream.of(
                Arguments.of(Connection.TRANSACTION_READ_UNCOMMITTED, true),
                Arguments.of(Connection.TRANSACTION_READ_COMMITTED, false),
                Arguments.of(Connection.TRANSACTION_REPEATABLE_READ, false),
                Arguments.of(Connection.TRANSACTION_SERIALIZABLE, false)
        );
    }

    /**
     * 격리 수준에 따라 어떤 현상이 발생하는지 테스트를 돌려 직접 눈으로 확인하고 표를 채워보자.
     * + : 발생
     * - : 발생하지 않음
     *   Read phenomena | Non-repeatable reads
     * Isolation level  |
     * -----------------|---------------------
     * Read Uncommitted | +
     * Read Committed   | +
     * Repeatable Read  | -
     * Serializable     | -
     */
    @Test
    void noneRepeatable() throws SQLException {
        setUp(createH2DataSource());
        userDao.insert(dataSource.getConnection(), new User("gugu", "password", "hkkang@woowahan.com"));
        Connection connection = dataSource.getConnection();

        connection.setAutoCommit(false);
        connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
        User userBefore = userDao.findByAccount(connection, "gugu");

        new Thread(RunnableWrapper.accept(() -> {
            Connection subConnection = dataSource.getConnection();
            User anotherUser = userDao.findByAccount(subConnection, "gugu");
            anotherUser.changePassword("qqqq");
            userDao.update(subConnection, anotherUser);
        })).start();

        sleep(0.5);
        User userAfter = userDao.findByAccount(connection, "gugu");
        assertThat(userAfter.getPassword().equals(userBefore.getPassword())).isTrue();
    }

    /**
     * phantom read는 h2에서 발생하지 않는다. mysql로 확인해보자.
     * 격리 수준에 따라 어떤 현상이 발생하는지 테스트를 돌려 직접 눈으로 확인하고 표를 채워보자.
     * + : 발생
     * - : 발생하지 않음
     *   Read phenomena | Phantom reads
     * Isolation level  |
     * -----------------|--------------
     * Read Uncommitted | +
     * Read Committed   | +
     * Repeatable Read  | +
     * Serializable     | -
     */
    @Test
    void phantomReading() throws SQLException {
        // testcontainer로 docker를 실행해서 mysql에 연결한다.
        MySQLContainer mysql = new MySQLContainer<>(DockerImageName.parse("mysql:8.0.30"))
                .withLogConsumer(new Slf4jLogConsumer(log));
        mysql.start();
        setUp(createMySQLDataSource(mysql));

        userDao.insert(dataSource.getConnection(), new User("gugu", "password", "hkkang@woowahan.com"));
        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(false);

        connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        userDao.findGreaterThan(connection, 1);

        new Thread(RunnableWrapper.accept(() -> {
            Connection subConnection = dataSource.getConnection();
            subConnection.setAutoCommit(false);
            userDao.insert(subConnection, new User("bird", "password", "bird@woowahan.com"));
            subConnection.commit();
        })).start();

        sleep(0.5);
        userDao.updatePasswordGreaterThan(connection, "qqqq", 1);

        List<User> actual = userDao.findGreaterThan(connection, 1);
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
