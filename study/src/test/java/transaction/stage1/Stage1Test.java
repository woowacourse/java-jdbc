package transaction.stage1;

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

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 격리 레벨(Isolation Level)에 따라 여러 사용자가 동시에 db에 접근했을 때 어떤 문제가 발생하는지 확인해보자.
 * ❗phantom reads는 docker를 실행한 상태에서 테스트를 실행한다.
 * ❗phantom reads는 MySQL로 확인한다. H2 데이터베이스에서는 발생하지 않는다.
 *
 * 참고 링크
 * https://en.wikipedia.org/wiki/Isolation_(database_systems)
 *
 * 각 테스트에서 어떤 현상이 발생하는지 직접 경험해보고 아래 표를 채워보자.
 * + : 발생
 * - : 발생하지 않음
 *   Read phenomena | Dirty reads | Non-repeatable reads | Phantom reads
 * Isolation level  |              |                      |
 * -----------------|--------------|----------------------|--------------
 * Read Uncommitted | +            | +                    | +
 * Read Committed   | -            | +                    | +
 * Repeatable Read  | -            | -                    | +
 * Serializable     | -            | -                    | -
 *
 * 이 표는 SQL 표준에서 설명하는 현상이다. 실제 결과는 DB 종류와 구현에 따라 달라질 수 있다.
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
    @Test
    void dirtyReading() throws SQLException {
        setUp(createH2DataSource());

        try (final var connection = dataSource.getConnection();
             final var subConnection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            userDao.insert(connection, new User("gugu", "password", "hkkang@woowahan.com"));

            final int isolationLevel = Connection.TRANSACTION_READ_UNCOMMITTED;
            subConnection.setTransactionIsolation(isolationLevel);
            final var actual = userDao.findByAccount(subConnection, "gugu");

            log.info("isolation level : {}, user : {}", isolationLevel, actual);
            assertThat(actual).isNotNull();

            // 사용자A가 롤백해도 사용자B가 본 데이터가 있었으므로 더티 리드다.
            connection.rollback();
        }
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

        try (final var setupConnection = dataSource.getConnection()) {
            userDao.insert(setupConnection, new User("gugu", "password", "hkkang@woowahan.com"));
        }

        try (final var connection = dataSource.getConnection();
             final var subConnection = dataSource.getConnection()) {
            final int isolationLevel = Connection.TRANSACTION_READ_COMMITTED;
            connection.setTransactionIsolation(isolationLevel);
            connection.setAutoCommit(false);

            final var user = userDao.findByAccount(connection, "gugu");
            log.info("user : {}", user);

            final var anotherUser = userDao.findByAccount(subConnection, "gugu");
            anotherUser.changePassword("qqqq");
            userDao.update(subConnection, anotherUser);

            final var actual = userDao.findByAccount(connection, "gugu");
            log.info("isolation level : {}, user : {}", isolationLevel, actual);
            assertThat(actual.getPassword()).isEqualTo("qqqq");

            connection.rollback();
        }
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

        try (final var mysql = new MySQLContainer<>(DockerImageName.parse("mysql:8.0.30"))
                .withLogConsumer(new Slf4jLogConsumer(log))) {
            mysql.start();
            try (final var mysqlDataSource = createMySQLDataSource(mysql)) {
                setUp(mysqlDataSource);

                try (final var setupConnection = dataSource.getConnection()) {
                    userDao.insert(setupConnection, new User("gugu", "password", "hkkang@woowahan.com"));
                }

                try (final var connection = dataSource.getConnection();
                     final var subConnection = dataSource.getConnection()) {
                    final int isolationLevel = Connection.TRANSACTION_READ_COMMITTED;
                    connection.setTransactionIsolation(isolationLevel);
                    connection.setAutoCommit(false);

                    userDao.findGreaterThan(connection, 1);

                    subConnection.setAutoCommit(false);
                    userDao.insert(subConnection, new User("bird", "password", "bird@woowahan.com"));
                    subConnection.commit();

                    // MySQL에서는 범위 UPDATE 후 다시 조회해 새 행이 결과에 나타나는지 확인한다.
                    userDao.updatePasswordGreaterThan(connection, "qqqq", 1);
                    final var actual = userDao.findGreaterThan(connection, 1);

                    log.info("isolation level : {}, user : {}", isolationLevel, actual);
                    assertThat(actual).hasSize(2);

                    connection.rollback();
                }
            }
        }
    }

    private static HikariDataSource createMySQLDataSource(final JdbcDatabaseContainer<?> container) {
        final var config = new HikariConfig();
        config.setJdbcUrl(container.getJdbcUrl() + "?allowMultiQueries=true");
        config.setUsername(container.getUsername());
        config.setPassword(container.getPassword());
        config.setDriverClassName(container.getDriverClassName());
        return new HikariDataSource(config);
    }

    private static DataSource createH2DataSource() {
        final var jdbcDataSource = new JdbcDataSource();
        // h2 로그를 확인하고 싶을 때 사용
//        jdbcDataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=3;MODE=MYSQL");
        jdbcDataSource.setUrl("jdbc:h2:mem:test" + UUID.randomUUID().toString().replace("-", "") + ";DB_CLOSE_DELAY=-1;MODE=MYSQL;");
        jdbcDataSource.setUser("sa");
        jdbcDataSource.setPassword("");
        return jdbcDataSource;
    }

}
