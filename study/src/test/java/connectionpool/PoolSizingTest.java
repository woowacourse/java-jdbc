package connectionpool;

import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Statistics;
import com.mysql.cj.jdbc.MysqlDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.DockerClientFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 같은 MySQL 조회에서 풀 크기와 연결 재사용에 따른 시간·트래픽을 비교하는 학습 실험.
 * 작은 데이터의 단순 조회 결과이므로 운영 서비스의 최적 풀 크기를 의미하지 않는다.
 */
class PoolSizingTest {

    private static final Logger log = LoggerFactory.getLogger(PoolSizingTest.class);
    private static final int WORKERS = 20;
    private static final int QUERIES_PER_WORKER = 50;
    private static final int ROUNDS = 3;

    @Test
    @Timeout(90)
    void compareNetworkTraffic() throws Exception {
        try (var mysql = new MySQLContainer<>("mysql:8.0.30").withDatabaseName("pool_traffic")) {
            mysql.start();
            try (var pool = createPool(mysql, 1)) {
                prepareData(pool);
                var direct = new MysqlDataSource();
                direct.setUrl(mysql.getJdbcUrl());
                direct.setUser(mysql.getUsername());
                direct.setPassword(mysql.getPassword());
                direct.setConnectTimeout(10_000);
                direct.setSocketTimeout(10_000);

                // 같은 SQL을 순차 1000회 실행하며, 연결 재사용 여부를 비교한다.
                measureTraffic(mysql, "noPooling", direct);
                measureTraffic(mysql, "pooling", pool);
            }
        }
    }

    private void measureTraffic(MySQLContainer<?> mysql, String mode, DataSource dataSource) throws Exception {
        var warmup = query(dataSource);
        if (warmup.failure() != null) {
            throw warmup.failure();
        }
        long before = networkBytes(mysql);
        long started = System.nanoTime();
        for (int i = 0; i < WORKERS * QUERIES_PER_WORKER; i++) {
            var sample = query(dataSource);
            if (sample.failure() != null) {
                throw sample.failure();
            }
        }
        long elapsed = System.nanoTime() - started;
        long transferred = networkBytes(mysql) - before;
        assertThat(transferred).as("container network bytes").isPositive();
        log.info("NETWORK mode={} completed=1000 failures=0 bytes={} totalMs={}",
                mode, transferred, elapsed / 1_000_000.0);
    }

    private long networkBytes(MySQLContainer<?> mysql) throws Exception {
        // 관리자 패킷 캡처 대신, 이 실험 전용 컨테이너의 송수신 누적 바이트를 읽는다.
        // 프로토콜별 패킷 분석이 아니라 컨테이너 전체 통계의 전후 차이다.
        var snapshot = new AtomicReference<Statistics>();
        try (var callback = new ResultCallback.Adapter<Statistics>() {
            @Override
            public void onNext(Statistics statistics) {
                snapshot.set(statistics);
            }
        }) {
            DockerClientFactory.instance().client().statsCmd(mysql.getContainerId())
                    .withNoStream(true).exec(callback);
            assertThat(callback.awaitCompletion(10, TimeUnit.SECONDS)).as("Docker stats received").isTrue();
        }
        var statistics = snapshot.get();
        assertThat(statistics).isNotNull();
        assertThat(statistics.getNetworks()).isNotEmpty();
        return statistics.getNetworks().values().stream()
                .mapToLong(network -> network.getRxBytes() + network.getTxBytes()).sum();
    }

    @Test
    @Timeout(120)
    void comparePoolSizes() throws Exception {
        // 컨테이너 시작, 데이터 준비, 풀 생성과 워밍업은 측정에서 제외한다.
        try (var mysql = new MySQLContainer<>("mysql:8.0.30").withDatabaseName("pool_sizing")) {
            mysql.start();
            try (var setupPool = createPool(mysql, 1)) {
                prepareData(setupPool);
            }

            var poolSizes = new ArrayList<>(List.of(1, 5, 10, 20));
            var random = new Random(20261019L);
            for (int round = 1; round <= ROUNDS; round++) {
                // 큰 풀이 항상 나중에 실행되지 않도록, 재현 가능한 순서로 섞는다.
                Collections.shuffle(poolSizes, random);
                for (int poolSize : poolSizes) {
                    try (var pool = createPool(mysql, poolSize)) {
                        fillPool(pool, poolSize);
                        var warmup = runWorkload(pool, 5);
                        assertThat(warmup.failures()).as("warmup failures").isZero();

                        var result = runWorkload(pool, QUERIES_PER_WORKER);
                        report(round, poolSize, result);
                        assertThat(result.samples()).hasSize(WORKERS * QUERIES_PER_WORKER);
                        assertThat(result.failures()).as("round %s, pool %s", round, poolSize).isZero();
                    }
                }
            }
        }
    }

    private HikariDataSource createPool(MySQLContainer<?> mysql, int poolSize) {
        var config = new HikariConfig();
        config.setJdbcUrl(mysql.getJdbcUrl());
        config.setUsername(mysql.getUsername());
        config.setPassword(mysql.getPassword());
        config.setMinimumIdle(poolSize);
        config.setMaximumPoolSize(poolSize);
        config.setConnectionTimeout(10_000);
        config.setAutoCommit(true);
        config.addDataSourceProperty("socketTimeout", "10000");
        return new HikariDataSource(config);
    }

    private void prepareData(DataSource dataSource) throws SQLException {
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE users (id INT PRIMARY KEY, email VARCHAR(100) NOT NULL)");
            statement.executeUpdate("INSERT INTO users VALUES (1, 'learner@example.com')");
        }
    }

    private void fillPool(DataSource pool, int poolSize) throws SQLException {
        // 설정한 수만큼 연결을 동시에 보유하고, 모두 반납한 뒤 측정을 준비한다.
        var connections = new ArrayList<Connection>();
        try {
            for (int i = 0; i < poolSize; i++) {
                connections.add(pool.getConnection());
            }
        } finally {
            for (var connection : connections) {
                connection.close();
            }
        }
    }

    private Result runWorkload(DataSource pool, int queriesPerWorker) throws Exception {
        var ready = new CountDownLatch(WORKERS);
        var start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(WORKERS)) {
            var futures = new ArrayList<Future<List<Sample>>>();
            for (int worker = 0; worker < WORKERS; worker++) {
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    start.await();
                    var samples = new ArrayList<Sample>();
                    for (int i = 0; i < queriesPerWorker; i++) {
                        samples.add(query(pool));
                    }
                    return samples;
                }));
            }

            // 스레드 생성 시간을 제외하고, 모두 준비된 뒤 동시에 출발시킨다.
            boolean allReady;
            long started;
            try {
                allReady = ready.await(10, TimeUnit.SECONDS);
                started = System.nanoTime();
            } finally {
                // 준비 대기가 중단돼도 작업이 출발 신호를 영원히 기다리지 않게 한다.
                start.countDown();
            }
            assertThat(allReady).as("all workers ready").isTrue();
            var samples = new ArrayList<Sample>();
            for (var future : futures) {
                // 작업 스레드의 예상치 못한 예외도 테스트 호출자에게 전달된다.
                samples.addAll(future.get());
            }
            return new Result(samples, System.nanoTime() - started);
        }
    }

    private Sample query(DataSource pool) {
        long started = System.nanoTime();
        long acquired = 0;
        long processed = 0;
        SQLException failure = null;
        try (var connection = pool.getConnection()) {
            acquired = System.nanoTime();
            try (var statement = connection.createStatement();
                 var rows = statement.executeQuery("SELECT email FROM users WHERE id = 1")) {
                if (!rows.next() || !"learner@example.com".equals(rows.getString(1))) {
                    throw new SQLException("Unexpected query result");
                }
            } finally {
                processed = System.nanoTime();
            }
        } catch (SQLException e) {
            // 실패를 버리지 않고 집계한 뒤, 해당 실험을 테스트 실패로 처리한다.
            failure = e;
        }
        long finished = System.nanoTime();
        return new Sample(acquired == 0 ? finished - started : acquired - started,
                acquired == 0 ? 0 : processed - acquired, finished - started, failure);
    }

    private void report(int round, int poolSize, Result result) {
        var successful = result.samples().stream().filter(sample -> sample.failure() == null).toList();
        log.info("POOL_SIZING round={} pool={} completed={} failures={} totalMs={} throughputPerSec={}",
                round, poolSize, successful.size(), result.failures(),
                result.elapsedNanos() / 1_000_000.0,
                successful.size() * 1_000_000_000.0 / result.elapsedNanos());
        if (!successful.isEmpty()) {
            log.info("POOL_LATENCY round={} pool={} waitP95Ms={} sqlP95Ms={} operationP95Ms={}",
                    round, poolSize,
                    p95(successful.stream().mapToLong(Sample::waitNanos).toArray()),
                    p95(successful.stream().mapToLong(Sample::sqlNanos).toArray()),
                    p95(successful.stream().mapToLong(Sample::totalNanos).toArray()));
        }
        result.samples().stream().filter(sample -> sample.failure() != null).findFirst()
                .ifPresent(sample -> log.error("First query failure", sample.failure()));
    }

    private double p95(long[] values) {
        java.util.Arrays.sort(values);
        return values[(int) Math.ceil(values.length * 0.95) - 1] / 1_000_000.0;
    }

    private record Sample(long waitNanos, long sqlNanos, long totalNanos, SQLException failure) {
    }

    private record Result(List<Sample> samples, long elapsedNanos) {
        long failures() {
            return samples.stream().filter(sample -> sample.failure() != null).count();
        }
    }
}
