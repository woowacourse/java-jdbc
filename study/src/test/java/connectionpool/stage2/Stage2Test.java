package connectionpool.stage2;

import static com.zaxxer.hikari.util.UtilityElf.quietlySleep;
import static org.assertj.core.api.Assertions.assertThat;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import connectionpool.DataSourceConfig;
import java.lang.reflect.Field;
import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class Stage2Test {

    private static final Logger log = LoggerFactory.getLogger(Stage2Test.class);

    /**
     * spring boot에서 설정 파일인 application.yml를 사용하여 DataSource를 설정할 수 있다. 하지만 DataSource를 여러 개 사용하거나 세부 설정을 하려면 빈을 직접 생성하는
     * 방법을 사용한다. DataSourceConfig 클래스를 찾아서 어떻게 빈으로 직접 생성하는지 확인해보자. 그리고 아래 DataSource가 직접 생성한 빈으로 주입 받았는지 getPoolName()
     * 메서드로 확인해보자.
     */
    @Autowired
    private DataSource dataSource;

    @Test
    void 여러_개_요청이_동시에_들어오면_커넥션_풀_개수_안에서_들어온_순서대로_커넥션을_할당받는다() throws InterruptedException {
        final var hikariDataSource = (HikariDataSource) dataSource;
        final HikariPool hikariPool = getPool((HikariDataSource) dataSource);

        /*
        설정한 커넥션 풀 최대값보다 더 많은 스레드를 생성해서 동시에 디비에 접근을 시도하면 어떻게 될까?
        처음 5개 스레드는 connection 요청하자마자 받음
        이후 요청한 스레드들은 connection이 반환될 때까지 대기하다 반환되면 하나씩 받음
        */
        final var threads = new Thread[20];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(getConnection(i));
        }

        for (final var thread : threads) {
            Thread.sleep(100); // 요청한 순서대로 connection 받는지 확인하고 싶어서, 스레드 요청 순서 꼬이지 않게 조절함
            thread.start(); // 스레드 각 요청간 텀 0.1초로 모든 스레드 요청 보내는데에 2초 걸림
        }

        for (final var thread : threads) {
            thread.join();
        }

        // 동시에 많은 요청이 몰려도 최대 풀 사이즈를 유지한다.
        assertThat(hikariPool.getTotalConnections()).isEqualTo(DataSourceConfig.MAXIMUM_POOL_SIZE);

        // DataSourceConfig 클래스에서 직접 생성한 커넥션 풀.
        assertThat(hikariDataSource.getPoolName()).isEqualTo("gugu");
    }

    // 학습 테스트를 위해 HikariPool을 추출
    private static HikariPool getPool(final HikariDataSource hikariDataSource) {
        try {
            Field field = hikariDataSource.getClass().getDeclaredField("pool");
            field.setAccessible(true);
            return (HikariPool) field.get(hikariDataSource);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    // 데이터베이스에 연결만 하는 메서드. 커넥션 풀에 몇 개의 연결이 생기는지 확인하는 용도.
    private Runnable getConnection(int i) {
        return () -> {
            try {
                log.info("Before acquire : " + i);
                try (Connection ignored = dataSource.getConnection()) {
                    log.info("After acquire : " + i);
                    quietlySleep(3000); // Thread.sleep(500)과 동일한 기능, 초단위로 로깅 보는 게 편해서 0.5초보다 크게 함
                } // + 모든 요청이 들어오고서 자원할당되는 거 순차적으로 보고 싶어서 3초로 함
                // (동시에 보낸다는 가정 유지하려면 맨 처음 요청이 커넥션 점유하고 있는 동안 모든 요청 들어와야 하기에 2초 이상이어야 함)
            } catch (Exception e) {
            }
        };
    }
}
