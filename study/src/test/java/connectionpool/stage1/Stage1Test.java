package connectionpool.stage1;

import static com.zaxxer.hikari.util.UtilityElf.quietlySleep;
import static org.assertj.core.api.Assertions.assertThat;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Stage1Test {

    private static final Logger log = LoggerFactory.getLogger(Stage1Test.class);
    private static final String H2_URL = "jdbc:h2:./test;DB_CLOSE_DELAY=-1";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    /**
     * 커넥션 풀링(Connection Pooling)이란? DataSource 객체를 통해 미리 커넥션(Connection)을 만들어 두는 것을 의미한다. 새로운 커넥션을 생성하는 것은 많은 비용이 들기에
     * 미리 커넥션을 만들어두면 성능상 이점이 있다. 커넥션 풀링에 미리 만들어둔 커넥션은 재사용 가능하다.
     * <p>
     * h2에서 제공하는 JdbcConnectionPool를 다뤄보며 커넥션 풀에 대한 감을 잡아보자.
     * <p>
     * Connection Pooling and Statement Pooling
     * https://docs.oracle.com/en/java/javase/11/docs/api/java.sql/javax/sql/package-summary.html
     */
    @Test
    void testJdbcConnectionPool() throws SQLException {
        final JdbcConnectionPool jdbcConnectionPool = JdbcConnectionPool.create(H2_URL, USER, PASSWORD);

        assertThat(jdbcConnectionPool.getActiveConnections()).isZero();
        try (final var connection = jdbcConnectionPool.getConnection()) {
            assertThat(connection.isValid(1)).isTrue();
            assertThat(jdbcConnectionPool.getActiveConnections()).isEqualTo(1);
        }
        assertThat(jdbcConnectionPool.getActiveConnections()).isZero();

        jdbcConnectionPool.dispose();
    }

    /**
     * Spring Boot 2.0 부터 HikariCP를 기본 데이터 소스로 채택하고 있다.
     * https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#data.sql.datasource.connection-pool
     * Supported Connection Pools We prefer HikariCP for its performance and concurrency. If HikariCP is available, we
     * always choose it.
     * <p>
     * HikariCP 공식 문서를 참고하여 HikariCP를 설정해보자. https://github.com/brettwooldridge/HikariCP#rocket-initialization
     * <p>
     * HikariCP 필수 설정 https://github.com/brettwooldridge/HikariCP#essentials
     * <p>
     * HikariCP의 pool size는 몇으로 설정하는게 좋을까? https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing
     * <p>
     * HikariCP를 사용할 때 적용하면 좋은 MySQL 설정 https://github.com/brettwooldridge/HikariCP/wiki/MySQL-Configuration
     */
    @Test
    void testHikariCP() {
        final var hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(H2_URL);
        hikariConfig.setUsername(USER);
        hikariConfig.setPassword(PASSWORD);
        hikariConfig.setMaximumPoolSize(5);
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        final var dataSource = new HikariDataSource(hikariConfig);
        final var properties = dataSource.getDataSourceProperties();

        assertThat(dataSource.getMaximumPoolSize()).isEqualTo(5);
        assertThat(properties.getProperty("cachePrepStmts")).isEqualTo("true");
        assertThat(properties.getProperty("prepStmtCacheSize")).isEqualTo("250");
        assertThat(properties.getProperty("prepStmtCacheSqlLimit")).isEqualTo("2048");

        dataSource.close();
    }

    /*
    Q) HikariCP의 pool size는 몇으로 설정하는게 좋을까? https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing
    pool size = 최대 스레드 수 * (단일 스레드의 최대 동시 연결 수 - 1) + 1 이상으로 설정해야 데드락 발생 안함
    if) 최대 스레드 수 = 8, 단일 스레드의 최대 동시 연결 수 = 1 -> pool size = 1
    if) 최대 스레드 수 = 8, 단일 스레드의 최대 동시 연결 수 = 2 -> pool size = 9
    if) 최대 스레드 수 = 8, 단일 스레드의 최대 동시 연결 수 = 3 -> pool size = 17

    Q) 왜 데드락이 발생할까?
    데드락은 둘 이상의 프로세스 또는 스레드들이 아무것도 진행하지 않는 상태로 서로 영원히 대기하는 상황을 말한다.
    데드락은 아래 4개 조건을 모두 만족하는 상태로 정의된다.
    1) 상호 배제: 반드시 한 번에 하나의 프로세스만이 해당 자원을 사용할 수 있는 경우
    2) 점유 상태로 대기: 2개 이상의 자원을 써야 하는 상황에서 아직 할당되지 않은 일부 작업 때문에 이미 할당된 작업을 반환하지 않고 기다리는 경우
         만일 점유 상태로 대기하는 일이 없다면, 기다리는 프로세스가 다른 자원을 갖고 있지 않으므로 순환성 대기가 발생할 수 없게 됨
         따라서 여러 자원을 동시에 할당받게 만들거나, 기다려야 하는 자원을 할당받으려면 다른 자원을 반환하도록 만들어 문제 해결할 수 있음
    3) 선점 불가: 다른 프로세스가 자원을 뺏어올 방법이 없는 경우
         우선순위 선점이 가능한 일부 상황에서만 해결할 수 있음 (os 레벨에서만 가능해 한정적임)
    4) 순환성 대기: 모든 프로세스가 다른 프로세스가 사용중인 자원을 기다리는 상황에서 마지막 프로세스가 첫 프로세스가 사용중인 자원을 쓰기 위해 대기중인 상황 (식사하는 철학자)

    Q) 커넥션 풀에선 데드락이 어떻게 발생할 수 있을까?
    최대 스레드 수인 8만큼 요청이 들어오는 상황에서 각 스레드가 2개의 커넥션을 필요로 하는 상황에서 커넥션 풀의 최대 크기가 8인 상황을 봐보자.
    00은 우선 8개의 스레드에 커넥션을 전부 1개씩 준다.
    모든 스레드가 1개의 커넥션을 더 필요로 하며 기다리는 상황에서 이미 풀 크기만큼의 커넥션을 전부 나눠줬기에 데드락이 발생한다.

    반면 같은 상황에서 커넥션 풀의 최대 크기가 9인 상황을 봐보자.
    이번에도 마찬가지로 00은 우선 8개의 스레드에 커넥션을 전부 1개식 준다.
    모든 스레드가 1개의 커넥션을 더 필요로 하며 기다리는 상황이란 점은 같지만, 아직 1개의 커넥션이 남아있기에 이를 할당 받은 커넥션이 작업을 완료할 수 있게 되어 데드락 발생 안함.

    Q) 커넥션 풀에서 데드락을 해결하려면 어떻게 해야 할까?
    모든 스레드가 커넥션을 받지 못하더라도 최소한 하나의 스레드는 작업을 완료할 수 있다면 데드락이 발생하지 않는다.
    HikariCP에서 제공하는 pool size 공식을 사용하면 순환성 대기가 끊어져 데드락이 해결된다.

    또 다른 방식으론 점유 상태로 대기하는 스레드를 만들지 않으면 데드락이 없어진다.
    이를 위해선 한 스레드가 여러 커넥션을 동시에 할당받아야만 하게끔 강제하거나, 스레드가 기다려야 하는 커넥션을 할당받으려면 다른 커넥션을 반환하도록 만들어 문제 해결할 수 있다.
    */
    @Test
    void testDeadlockScenario() {
        final var hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(H2_URL);
        hikariConfig.setUsername(USER);
        hikariConfig.setPassword(PASSWORD);
        hikariConfig.setMaximumPoolSize(9); // pool size = 8 x (2 - 1) + 1 = 8 x 1 + 1 = 9 이상으로 해야 데드락 안남
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        final HikariDataSource dataSource = new HikariDataSource(hikariConfig);

        // 8개의 스레드 풀 생성
        ExecutorService executorService = Executors.newFixedThreadPool(8);

        // 각 스레드는 3개의 커넥션을 요청
        for (int i = 0; i < 8; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    log.info(index + "번째 스레드 요청 시작");
                    performTaskWithMultipleConnections(dataSource, index);
                    log.info(index + "번째 스레드 요청 종료");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
            // quietlySleep(100); // 넣으면 동시 요청 말고 순차적으로 요청 보내는 경우 테스트 가능. 순차적으로 보내면 최대 스레드 수=1 로 계산하면 됨
        }

        // 모든 작업이 완료될 때까지 대기
        executorService.shutdown();
        while (!executorService.isTerminated()) {
            quietlySleep(1000); // 스레드 풀이 종료될 때까지 대기
        }

        // 모든 작업 완료되고서 dataSource close
        dataSource.close();
    }

    private void performTaskWithMultipleConnections(HikariDataSource dataSource, int index) throws SQLException {
        Connection connection1 = dataSource.getConnection();
        log.info(index + "번째 스레드의 첫 번째 connection 할당 완료");
        // 여기서 connection1 close 하면 데드락 발생 안함

        Connection connection2 = dataSource.getConnection();
        quietlySleep(1000); // 넣으면 더 천천히 자원 할당되는 과정 볼 수 있음 (두번째 자원 할당받고 해제하는 로그 합쳐서 보고자 이곳에 위치시킴)
        log.info(index + "번째 스레드의 두 번째 connection 할당 완료");

        connection1.close();
        connection2.close();
    }
}

/*
16:44:31.758 [pool-1-thread-3] INFO connectionpool.stage1.Stage1Test -- 2번째 스레드 요청 시작
16:44:31.758 [pool-1-thread-5] INFO connectionpool.stage1.Stage1Test -- 4번째 스레드 요청 시작
16:44:31.758 [pool-1-thread-1] INFO connectionpool.stage1.Stage1Test -- 0번째 스레드 요청 시작
16:44:31.758 [pool-1-thread-7] INFO connectionpool.stage1.Stage1Test -- 6번째 스레드 요청 시작
16:44:31.758 [pool-1-thread-6] INFO connectionpool.stage1.Stage1Test -- 5번째 스레드 요청 시작
16:44:31.758 [pool-1-thread-4] INFO connectionpool.stage1.Stage1Test -- 3번째 스레드 요청 시작
16:44:31.758 [pool-1-thread-2] INFO connectionpool.stage1.Stage1Test -- 1번째 스레드 요청 시작
16:44:31.759 [pool-1-thread-8] INFO connectionpool.stage1.Stage1Test -- 7번째 스레드 요청 시작
16:44:31.764 [pool-1-thread-5] INFO connectionpool.stage1.Stage1Test -- 4번째 스레드의 첫 번째 connection 할당 완료
16:44:31.766 [pool-1-thread-4] INFO connectionpool.stage1.Stage1Test -- 3번째 스레드의 첫 번째 connection 할당 완료
16:44:31.799 [pool-1-thread-6] INFO connectionpool.stage1.Stage1Test -- 5번째 스레드의 첫 번째 connection 할당 완료
16:44:31.835 [pool-1-thread-1] INFO connectionpool.stage1.Stage1Test -- 0번째 스레드의 첫 번째 connection 할당 완료
16:44:31.871 [pool-1-thread-3] INFO connectionpool.stage1.Stage1Test -- 2번째 스레드의 첫 번째 connection 할당 완료
16:44:31.905 [pool-1-thread-2] INFO connectionpool.stage1.Stage1Test -- 1번째 스레드의 첫 번째 connection 할당 완료
16:44:31.941 [pool-1-thread-7] INFO connectionpool.stage1.Stage1Test -- 6번째 스레드의 첫 번째 connection 할당 완료
16:44:31.977 [pool-1-thread-8] INFO connectionpool.stage1.Stage1Test -- 7번째 스레드의 첫 번째 connection 할당 완료
16:44:33.018 [pool-1-thread-5] INFO connectionpool.stage1.Stage1Test -- 4번째 스레드의 두 번째 connection 할당 완료 [1개 남은 connection 할당받음]
16:44:33.031 [pool-1-thread-5] INFO connectionpool.stage1.Stage1Test -- 4번째 스레드 요청 종료                    [connection 2개 풀림]
16:44:34.036 [pool-1-thread-4] INFO connectionpool.stage1.Stage1Test -- 3번째 스레드의 두 번째 connection 할당 완료 [2개 풀린 connection 하나씩 할당받음]
16:44:34.036 [pool-1-thread-6] INFO connectionpool.stage1.Stage1Test -- 5번째 스레드의 두 번째 connection 할당 완료 [2개 풀린 connection 하나씩 할당받음]
16:44:34.036 [pool-1-thread-4] INFO connectionpool.stage1.Stage1Test -- 3번째 스레드 요청 종료                    [connection 2개 풀림]
16:44:34.036 [pool-1-thread-6] INFO connectionpool.stage1.Stage1Test -- 5번째 스레드 요청 종료                    [connection 총 4개 풀림]
16:44:35.039 [pool-1-thread-2] INFO connectionpool.stage1.Stage1Test -- 1번째 스레드의 두 번째 connection 할당 완료
16:44:35.039 [pool-1-thread-3] INFO connectionpool.stage1.Stage1Test -- 2번째 스레드의 두 번째 connection 할당 완료
16:44:35.039 [pool-1-thread-7] INFO connectionpool.stage1.Stage1Test -- 6번째 스레드의 두 번째 connection 할당 완료
16:44:35.039 [pool-1-thread-1] INFO connectionpool.stage1.Stage1Test -- 0번째 스레드의 두 번째 connection 할당 완료
16:44:35.039 [pool-1-thread-2] INFO connectionpool.stage1.Stage1Test -- 1번째 스레드 요청 종료
16:44:35.039 [pool-1-thread-7] INFO connectionpool.stage1.Stage1Test -- 6번째 스레드 요청 종료
16:44:35.039 [pool-1-thread-1] INFO connectionpool.stage1.Stage1Test -- 0번째 스레드 요청 종료
16:44:35.040 [pool-1-thread-3] INFO connectionpool.stage1.Stage1Test -- 2번째 스레드 요청 종료
16:44:36.041 [pool-1-thread-8] INFO connectionpool.stage1.Stage1Test -- 7번째 스레드의 두 번째 connection 할당 완료
16:44:36.042 [pool-1-thread-8] INFO connectionpool.stage1.Stage1Test -- 7번째 스레드 요청 종료
*/
