package connectionpool.stage2;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import connectionpool.DataSourceConfig;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;

import static com.zaxxer.hikari.util.UtilityElf.quietlySleep;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class Stage2Test {

    private static final Logger log = LoggerFactory.getLogger(Stage2Test.class);

    /**
     * spring boot에서 설정 파일인 application.yml를 사용하여 DataSource를 설정할 수 있다.
     * 하지만 DataSource를 여러 개 사용하거나 세부 설정을 하려면 빈을 직접 생성하는 방법을 사용한다.
     * DataSourceConfig 클래스를 찾아서 어떻게 빈으로 직접 생성하는지 확인해보자.
     * 그리고 아래 DataSource가 직접 생성한 빈으로 주입 받았는지 getPoolName() 메서드로 확인해보자.
     */
    /**
     * 실제 로그 확인해보면
     * 2025-10-05T17:34:10.398+09:00  INFO 42458 --- [       Thread-5] connectionpool.stage2.Stage2Test         : Before acquire
     * 2025-10-05T17:34:10.398+09:00  INFO 42458 --- [       Thread-8] connectionpool.stage2.Stage2Test         : Before acquire
     * 2025-10-05T17:34:10.398+09:00  INFO 42458 --- [       Thread-6] connectionpool.stage2.Stage2Test         : Before acquire
     * 2025-10-05T17:34:10.398+09:00  INFO 42458 --- [       Thread-9] connectionpool.stage2.Stage2Test         : Before acquire
     * 2025-10-05T17:34:10.398+09:00  INFO 42458 --- [      Thread-10] connectionpool.stage2.Stage2Test         : Before acquire
     * 2025-10-05T17:34:10.398+09:00  INFO 42458 --- [      Thread-11] connectionpool.stage2.Stage2Test         : Before acquire
     * 2025-10-05T17:34:10.398+09:00  INFO 42458 --- [       Thread-7] connectionpool.stage2.Stage2Test         : Before acquire
     * 2025-10-05T17:34:10.398+09:00  INFO 42458 --- [      Thread-12] connectionpool.stage2.Stage2Test         : Before acquire
     * 2025-10-05T17:34:10.398+09:00  INFO 42458 --- [      Thread-13] connectionpool.stage2.Stage2Test         : Before acquire
     * 2025-10-05T17:34:10.398+09:00  INFO 42458 --- [       Thread-5] connectionpool.stage2.Stage2Test         : After acquire
     * 2025-10-05T17:34:10.398+09:00  INFO 42458 --- [      Thread-14] connectionpool.stage2.Stage2Test         : Before acquire
     * 2025-10-05T17:34:10.398+09:00  INFO 42458 --- [       Thread-6] connectionpool.stage2.Stage2Test         : After acquire
     * 2025-10-05T17:34:10.398+09:00  INFO 42458 --- [       Thread-8] connectionpool.stage2.Stage2Test         : After acquire
     * 2025-10-05T17:34:10.398+09:00  INFO 42458 --- [      Thread-10] connectionpool.stage2.Stage2Test         : After acquire
     * 2025-10-05T17:34:10.398+09:00  INFO 42458 --- [      Thread-15] connectionpool.stage2.Stage2Test         : Before acquire
     * 2025-10-05T17:34:10.398+09:00  INFO 42458 --- [       Thread-9] connectionpool.stage2.Stage2Test         : After acquire
     * 2025-10-05T17:34:10.399+09:00  INFO 42458 --- [      Thread-22] connectionpool.stage2.Stage2Test         : Before acquire
     * 2025-10-05T17:34:10.398+09:00  INFO 42458 --- [      Thread-16] connectionpool.stage2.Stage2Test         : Before acquire
     * 2025-10-05T17:34:10.398+09:00  INFO 42458 --- [      Thread-17] connectionpool.stage2.Stage2Test         : Before acquire
     * 2025-10-05T17:34:10.399+09:00  INFO 42458 --- [      Thread-18] connectionpool.stage2.Stage2Test         : Before acquire
     * 2025-10-05T17:34:10.399+09:00  INFO 42458 --- [      Thread-19] connectionpool.stage2.Stage2Test         : Before acquire
     * 2025-10-05T17:34:10.399+09:00  INFO 42458 --- [      Thread-20] connectionpool.stage2.Stage2Test         : Before acquire
     * 2025-10-05T17:34:10.399+09:00  INFO 42458 --- [      Thread-21] connectionpool.stage2.Stage2Test         : Before acquire
     * 2025-10-05T17:34:10.399+09:00  INFO 42458 --- [      Thread-23] connectionpool.stage2.Stage2Test         : Before acquire
     * 2025-10-05T17:34:10.399+09:00  INFO 42458 --- [      Thread-24] connectionpool.stage2.Stage2Test         : Before acquire
     * 2025-10-05T17:34:10.900+09:00  INFO 42458 --- [      Thread-11] connectionpool.stage2.Stage2Test         : After acquire
     * 2025-10-05T17:34:10.903+09:00  INFO 42458 --- [       Thread-7] connectionpool.stage2.Stage2Test         : After acquire
     * 2025-10-05T17:34:10.904+09:00  INFO 42458 --- [      Thread-13] connectionpool.stage2.Stage2Test         : After acquire
     * 2025-10-05T17:34:10.904+09:00  INFO 42458 --- [      Thread-14] connectionpool.stage2.Stage2Test         : After acquire
     * 2025-10-05T17:34:10.904+09:00  INFO 42458 --- [      Thread-12] connectionpool.stage2.Stage2Test         : After acquire
     * 2025-10-05T17:34:11.402+09:00  INFO 42458 --- [      Thread-15] connectionpool.stage2.Stage2Test         : After acquire
     * 2025-10-05T17:34:11.407+09:00  INFO 42458 --- [      Thread-22] connectionpool.stage2.Stage2Test         : After acquire
     * 2025-10-05T17:34:11.408+09:00  INFO 42458 --- [      Thread-16] connectionpool.stage2.Stage2Test         : After acquire
     * 2025-10-05T17:34:11.409+09:00  INFO 42458 --- [      Thread-17] connectionpool.stage2.Stage2Test         : After acquire
     * 2025-10-05T17:34:11.409+09:00  INFO 42458 --- [      Thread-18] connectionpool.stage2.Stage2Test         : After acquire
     * 2025-10-05T17:34:11.907+09:00  INFO 42458 --- [      Thread-19] connectionpool.stage2.Stage2Test         : After acquire
     * 2025-10-05T17:34:11.910+09:00  INFO 42458 --- [      Thread-23] connectionpool.stage2.Stage2Test         : After acquire
     * 2025-10-05T17:34:11.910+09:00  INFO 42458 --- [      Thread-20] connectionpool.stage2.Stage2Test         : After acquire
     * 2025-10-05T17:34:11.910+09:00  INFO 42458 --- [      Thread-21] connectionpool.stage2.Stage2Test         : After acquire
     * 2025-10-05T17:34:11.912+09:00  INFO 42458 --- [      Thread-24] connectionpool.stage2.Stage2Test         : After acquire
     *
     * 17:34:10.900전까지 모든 스레드에서 before acquire를 찍음.
     * 그리고 실제 connection을 획든한 스레드는 5개뿐.
     * 실제 커넥션 풀의 maxSize 5가 제대로 설정된 것을 확인할 수 있다.
     * 이후에도 5개씩 acquire가 찍힌 것을 확인할 수 있다.
     */
    @Autowired
    private DataSource dataSource;

    @Test
    void test() throws InterruptedException {
        final var hikariDataSource = (HikariDataSource) dataSource;
        final var hikariPool = getPool((HikariDataSource) dataSource);

        // 설정한 커넥션 풀 최대값보다 더 많은 스레드를 생성해서 동시에 디비에 접근을 시도하면 어떻게 될까?
        final var threads = new Thread[20];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(getConnection());
        }

        for (final var thread : threads) {
            thread.start();
        }

        for (final var thread : threads) {
            thread.join();
        }

        // 동시에 많은 요청이 몰려도 최대 풀 사이즈를 유지한다.
        assertThat(hikariPool.getTotalConnections()).isEqualTo(DataSourceConfig.MAXIMUM_POOL_SIZE);

        // DataSourceConfig 클래스에서 직접 생성한 커넥션 풀.
        assertThat(hikariDataSource.getPoolName()).isEqualTo("gugu");
    }

    // 데이터베이스에 연결만 하는 메서드. 커넥션 풀에 몇 개의 연결이 생기는지 확인하는 용도.
    private Runnable getConnection() {
        return () -> {
            try {
                log.info("Before acquire ");
                try (Connection ignored = dataSource.getConnection()) {
                    log.info("After acquire ");
                    quietlySleep(500); // Thread.sleep(500)과 동일한 기능
                }
            } catch (Exception e) {
            }
        };
    }

    // 학습 테스트를 위해 HikariPool을 추출
    public static HikariPool getPool(final HikariDataSource hikariDataSource)
    {
        try {
            Field field = hikariDataSource.getClass().getDeclaredField("pool");
            field.setAccessible(true);
            return (HikariPool) field.get(hikariDataSource);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
