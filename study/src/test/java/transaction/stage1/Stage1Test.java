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
import transaction.RunnableWrapper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

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
 * Isolation level  |             |                      |
 * -----------------|-------------|----------------------|--------------
 * Read Uncommitted |     +       |          +           |       +
 * Read Committed   |     -       |          +           |       +
 * Repeatable Read  |     -       |          -           |       +
 * Serializable     |     -       |          -           |       -
 */
class Stage1Test {

    private static final Logger log = LoggerFactory.getLogger(Stage1Test.class);
    private DataSource dataSource;
    private UserDao userDao;

    private void setUp(final DataSource dataSource) {
        this.dataSource = dataSource;
        DatabasePopulatorUtils.execute(dataSource); // schema.sql을 실행시킨다.
        this.userDao = new UserDao(dataSource);
    }

    /**
     * 격리 수준에 따라 어떤 현상이 발생하는지 테스트를 돌려 직접 눈으로 확인하고 표를 채워보자.
     * -> A트랜잭션이 테이블의 값을 변경하고 아직 커밋하지 않은 시점에서 B트랜잭션이 해당 값을 read했을 때
     * 변경된 값이 읽어지는 현상이 Dirty reads다. ReadCommitted부터는 커밋되지 않은 값을 read시 읽어오지 않으므로
     * Dirty reads가 발생하지 않는다.
     * + : 발생
     * - : 발생하지 않음
     *   Read phenomena | Dirty reads
     * Isolation level  |
     * -----------------|-------------
     * Read Uncommitted |     +
     * Read Committed   |     -
     * Repeatable Read  |     -
     * Serializable     |     -
     */
    @Test
    void dirtyReading() throws SQLException {
        setUp(createH2DataSource());

        // db에 새로운 연결(사용자A)을 받아와서
        final var connection = dataSource.getConnection();

        // 트랜잭션을 시작한다.
        connection.setAutoCommit(false);

        // db에 데이터를 추가하고 커밋하기 전에
        userDao.insert(connection, new User("gugu", "password", "hkkang@woowahan.com"));

        new Thread(RunnableWrapper.accept(() -> {
            // db에 connection(사용자A)이 아닌 새로운 연결인 subConnection(사용자B)을 받아온다.
            final var subConnection = dataSource.getConnection();

            // 적절한 격리 레벨을 찾는다.
            final int isolationLevel = Connection.TRANSACTION_READ_COMMITTED;

            // 트랜잭션 격리 레벨을 설정한다.
            subConnection.setTransactionIsolation(isolationLevel);

            // ❗️gugu 객체는 connection에서 아직 커밋하지 않은 상태다.
            // 격리 레벨에 따라 커밋하지 않은 gugu 객체를 조회할 수 있다.
            // 사용자B가 사용자A가 커밋하지 않은 데이터를 조회하는게 적절할까?
            // -> 적절하지 않다. 사용자A의 트랜잭션이 롤백된다면, gugu 객체는 실제로 존재하지 않는 값이 되기 때문이다.
            final var actual = userDao.findByAccount(subConnection, "gugu");

            // 트랜잭션 격리 레벨에 따라 아래 테스트가 통과한다.
            // 어떤 격리 레벨일 때 다른 연결의 커밋 전 데이터를 조회할 수 있을지 찾아보자. -> Read uncommitted
            // 다른 격리 레벨은 어떤 결과가 나오는지 직접 확인해보자.
            log.info("isolation level : {}, user : {}", isolationLevel, actual);
            assertThat(actual).isNull();
        })).start();

        sleep(0.5);

        // 롤백하면 사용자A의 user 데이터를 저장하지 않았는데 사용자B는 user 데이터가 있다고 인지한 상황이 된다. -> Read uncommitted 일 때 발생하는 현상
        connection.rollback();
    }

    /**
     * 격리 수준에 따라 어떤 현상이 발생하는지 테스트를 돌려 직접 눈으로 확인하고 표를 채워보자.
     * + : 발생
     * - : 발생하지 않음
     *   Read phenomena | Non-repeatable reads
     * Isolation level  |
     * -----------------|---------------------
     * Read Uncommitted |          +
     * Read Committed   |          +
     * Repeatable Read  |          -
     * Serializable     |          -
     */
    @Test
    void noneRepeatable() throws SQLException {
        setUp(createH2DataSource());

        // 테스트 전에 필요한 데이터를 추가한다.
        userDao.insert(dataSource.getConnection(), new User("gugu", "password", "hkkang@woowahan.com"));

        // db에 새로운 연결(사용자A)을 받아와서
        final var connection = dataSource.getConnection();

        // 트랜잭션을 시작한다.
        connection.setAutoCommit(false);

        // 적절한 격리 레벨을 찾는다.
        final int isolationLevel = Connection.TRANSACTION_REPEATABLE_READ;

        // 트랜잭션 격리 레벨을 설정한다.
        connection.setTransactionIsolation(isolationLevel);

        // 사용자A가 gugu 객체를 조회했다.
        final var user = userDao.findByAccount(connection, "gugu");
        log.info("user : {}", user);

        new Thread(RunnableWrapper.accept(() -> {
            // 사용자B가 새로 연결하여
            final var subConnection = dataSource.getConnection();

            // 사용자A가 조회한 gugu 객체를 사용자B가 다시 조회했다.
            final var anotherUser = userDao.findByAccount(subConnection, "gugu");

            // ❗사용자B가 gugu 객체의 비밀번호를 변경했다.(subConnection은 auto commit 상태)
            anotherUser.changePassword("qqqq");
            userDao.update(subConnection, anotherUser);
        })).start();

        sleep(0.5);

        // 사용자A가 다시 gugu 객체를 조회했다.
        final var actual = userDao.findByAccount(connection, "gugu");

        // 트랜잭션 격리 레벨에 따라 아래 테스트가 통과한다.
        // 각 격리 레벨은 어떤 결과가 나오는지 직접 확인해보자.
        /*
        변경된 패스워드가 읽어지면, Non-repeatable reads 현상이 발생한다고 본다.
        하나의 트랜잭션이 작업하는 동안에도 다른 트랜잭션의 커밋에 영향을 받아 반복된 read 작업을 할 때마다 값이 달라질 수 있는 현상이다.
        하나의 트랜잭션에서 같은 데이터나 테이블을 여러 번 조회해야 할 때, 데이터 정합성이 중요한 요소라면 Repeatable read가 보장되어야 한다.
         */
        log.info("isolation level : {}, user : {}", isolationLevel, actual);
        assertThat(actual.getPassword()).isEqualTo("password");

        connection.rollback();
    }

    /**
     * phantom read는 h2에서 발생하지 않는다. mysql로 확인해보자.
     * phantom read : 특정 트랜잭션이 실행되는 동안, 다른 트랜잭션이 데이터를 삽입하거나 삭제하여 첫 번째 트랜잭션이 결과를 조회할 때 예상과 다른 결과를 얻게 되는 현상
     * 격리 수준에 따라 어떤 현상이 발생하는지 테스트를 돌려 직접 눈으로 확인하고 표를 채워보자.
     * + : 발생
     * - : 발생하지 않음
     * Read phenomena | Phantom reads
     * Isolation level  |
     * -----------------|--------------
     * Read Uncommitted |       +
     * Read Committed   |       +
     * Repeatable Read  |       +
     * Serializable     |       -
     */
    @Test
    void phantomReading() throws SQLException {

        // testcontainer로 docker를 실행해서 mysql에 연결한다.
        final var mysql = new MySQLContainer<>(DockerImageName.parse("mysql:8.0.30"))
                .withLogConsumer(new Slf4jLogConsumer(log));
        mysql.start();
        setUp(createMySQLDataSource(mysql));

        // 테스트 전에 필요한 데이터를 추가한다.
        userDao.insert(dataSource.getConnection(), new User("gugu", "password", "hkkang@woowahan.com"));

        // db에 새로운 연결(사용자A)을 받아와서
        final var connection = dataSource.getConnection();

        // 트랜잭션을 시작한다.
        connection.setAutoCommit(false);

        // 적절한 격리 레벨을 찾는다.
        final int isolationLevel = Connection.TRANSACTION_SERIALIZABLE;

        // 트랜잭션 격리 레벨을 설정한다.
        connection.setTransactionIsolation(isolationLevel);

        // 사용자A가 id로 범위를 조회했다.
        userDao.findGreaterThan(connection, 1);

        new Thread(RunnableWrapper.accept(() -> {
            // 사용자B가 새로 연결하여
            final var subConnection = dataSource.getConnection();

            // 트랜잭션 시작
            subConnection.setAutoCommit(false);

            // 새로운 user 객체를 저장했다.
            // id는 2로 저장된다.
            userDao.insert(subConnection, new User("bird", "password", "bird@woowahan.com"));

            subConnection.commit();
        })).start();

        sleep(0.5);

        // MySQL에서 팬텀 읽기를 시연하려면 update를 실행해야 한다.
        // http://stackoverflow.com/questions/42794425/unable-to-produce-a-phantom-read/42796969#42796969
        userDao.updatePasswordGreaterThan(connection, "qqqq", 1);

        // 사용자A가 다시 id로 범위를 조회했다.
        final var actual = userDao.findGreaterThan(connection, 1);

        // 트랜잭션 격리 레벨에 따라 아래 테스트가 통과한다.
        // 각 격리 레벨은 어떤 결과가 나오는지 직접 확인해보자.
        // Serializable의 경우에만 1이 나오고, 그 외에는 2개의 데이터가 조회된다. (bird도 조회된다)
        log.info("isolation level : {}, user : {}", isolationLevel, actual);
        assertThat(actual).hasSize(1);

        connection.rollback();
        mysql.close();
    }

    private static DataSource createMySQLDataSource(final JdbcDatabaseContainer<?> container) {
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
