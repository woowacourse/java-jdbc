package connectionpool;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class PoolSizingTest {

    private static final int THREADS = 32;
    private static final int REQUESTS = 10_000;
    private static final int ROUNDS = 5;

    private static final int[] POOL_SIZES = {1, 2, 4, 8, 16, 32};

    record Sample(
            double waitMs,
            double sqlMs,
            double totalMs
    ) {
    }

    @Test
    void comparePoolSizes() throws Exception {
        for (int round = 1; round <= ROUNDS; round++) {
            System.out.println("\n=== Round " + round + " ===");

            for (int size : POOL_SIZES) {
                benchmark(size);
            }
        }
    }

    private void benchmark(int size) throws Exception {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl("jdbc:h2:mem:bench;DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");

        config.setMaximumPoolSize(size);
        config.setMinimumIdle(size);
        config.setConnectionTimeout(10_000);

        try (HikariDataSource pool = new HikariDataSource(config)) {

            // 1. Connection 사전 생성
            prepareConnections(pool, size);

            // 2. 워밍업
            for (int i = 0; i < 100; i++) {
                query(pool);
            }

            // 3. 동시 요청 실행
            ExecutorService executor =
                    Executors.newFixedThreadPool(THREADS);

            try {
                List<Future<Sample>> futures = new ArrayList<>();

                long start = System.nanoTime();

                for (int i = 0; i < REQUESTS; i++) {
                    futures.add(executor.submit(() -> query(pool)));
                }

                List<Sample> samples = new ArrayList<>();

                for (Future<Sample> future : futures) {
                    samples.add(future.get());
                }

                long end = System.nanoTime();

                // 4. TPS 계산
                double seconds = (end - start) / 1e9;
                double throughput = REQUESTS / seconds;

                // 5. 구간별 p95 계산
                double waitP95 = p95(
                        samples.stream()
                                .map(Sample::waitMs)
                                .toList()
                );

                double sqlP95 = p95(
                        samples.stream()
                                .map(Sample::sqlMs)
                                .toList()
                );

                double totalP95 = p95(
                        samples.stream()
                                .map(Sample::totalMs)
                                .toList()
                );

                // 6. 결과 출력
                System.out.printf(
                        "pool=%2d TPS=%9.1f waitP95=%.3fms sqlP95=%.3fms totalP95=%.3fms%n",
                        size,
                        throughput,
                        waitP95,
                        sqlP95,
                        totalP95
                );

            } finally {
                executor.shutdownNow();
            }
        }
    }

    private void prepareConnections(
            HikariDataSource pool,
            int size
    ) throws SQLException {

        List<Connection> connections = new ArrayList<>();

        try {
            for (int i = 0; i < size; i++) {
                connections.add(pool.getConnection());
            }
        } finally {
            for (Connection connection : connections) {
                connection.close();
            }
        }
    }

    private Sample query(HikariDataSource pool) throws SQLException {
        long start = System.nanoTime();

        long acquired;
        long sqlDone;

        try (Connection connection = pool.getConnection()) {
            acquired = System.nanoTime();

            try (var statement = connection.createStatement();
                 var rs = statement.executeQuery(
                         "SELECT SUM(X) " +
                                 "FROM SYSTEM_RANGE(1, 100000) " +
                                 "WHERE MOD(X, 7) = 0"
                 )) {

                if (!rs.next()) {
                    throw new SQLException("Query returned no rows");
                }

                rs.getLong(1);
            }

            sqlDone = System.nanoTime();
        }

        long finished = System.nanoTime();

        return new Sample(
                (acquired - start) / 1e6,
                (sqlDone - acquired) / 1e6,
                (finished - start) / 1e6
        );
    }

    private double p95(List<Double> values) {
        List<Double> sorted = values.stream()
                .sorted()
                .toList();

        int index = (int) Math.ceil(sorted.size() * 0.95) - 1;

        return sorted.get(index);
    }
}
