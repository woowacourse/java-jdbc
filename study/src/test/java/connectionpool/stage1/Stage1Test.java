package connectionpool.stage1;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class Stage1Test {

    private static final String H2_URL = "jdbc:h2:./test;DB_CLOSE_DELAY=-1";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    /**
     * 커넥션 풀링(Connection Pooling)이란?
     * DataSource 객체를 통해 미리 커넥션(Connection)을 만들어 두는 것을 의미한다.
     * 새로운 커넥션을 생성하는 것은 많은 비용이 들기에 미리 커넥션을 만들어두면 성능상 이점이 있다.
     * 커넥션 풀링에 미리 만들어둔 커넥션은 재사용 가능하다.
     *
     * h2에서 제공하는 JdbcConnectionPool를 다뤄보며 커넥션 풀에 대한 감을 잡아보자.
     *
     * Connection Pooling and Statement Pooling
     * https://docs.oracle.com/en/java/javase/11/docs/api/java.sql/javax/sql/package-summary.html
     */
    @Test
    void testJdbcConnectionPool() throws SQLException {
        final JdbcConnectionPool jdbcConnectionPool = JdbcConnectionPool.create(H2_URL, USER, PASSWORD);

        // 활성화된 커넥션이 없음을 확인
        // activeConnection: 데이터베이스와의 연결이 현재 활성화되어 사용되고 있는 커넥션
        assertThat(jdbcConnectionPool.getActiveConnections()).isZero();
        // 커넥션 풀에서 새로운 커넥션을 요청
        try (final var connection = jdbcConnectionPool.getConnection()) {
            // 커넥션 유효성 확인
            assertThat(connection.isValid(1)).isTrue();
            // 호라성 커넥션 수가 증가했는지 확인
            assertThat(jdbcConnectionPool.getActiveConnections()).isEqualTo(1);
        }
        // 커넥션이 종료된 후, 자원이 올바르게 반환되었음을 검증
        assertThat(jdbcConnectionPool.getActiveConnections()).isZero();

        // 커넥션 풀 종료
        jdbcConnectionPool.dispose();
    }

    /**
     * Spring Boot 2.0 부터 HikariCP를 기본 데이터 소스로 채택하고 있다.
     * https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#data.sql.datasource.connection-pool
     * Supported Connection Pools
     * We prefer HikariCP for its performance and concurrency. If HikariCP is available, we always choose it.
     * 우선순위: HikariCP > Tomcat > DBCP2 > Oracle UCP
     *
     * HikariCP 공식 문서를 참고하여 HikariCP를 설정해보자.
     * https://github.com/brettwooldridge/HikariCP#rocket-initialization
     *
     * HikariCP 필수 설정
     * https://github.com/brettwooldridge/HikariCP#essentials
     * dataSourceClassName or jdbcUrl, username, password
     *
     * HikariCP의 pool size는 몇으로 설정하는게 좋을까?
     * https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing
     *
     * The Formula provided by the PostgreSQL project: connections = ((core_count * 2) + effective_spindle_count)
     * Axiom: You want a small pool, saturated with threads waiting for connections.
     *  - connection pool의 크기를 작게 유지하고 나머지 스레드는 connection을 기다리게 하는 것이 오히려 성능에 이점을 줄 수 있다.
     *Pool sizing is ultimately very specific to deployments.
     *  - 각 시스템의 특정에 따라 pool size를 조정해야 하고, 실제 부하를 시뮬레이션하고 다양한 pool 설정을 테스트해야 한다.
     *
     * HikariCP를 사용할 때 적용하면 좋은 MySQL 설정
     * https://github.com/brettwooldridge/HikariCP/wiki/MySQL-Configuration
     */
    @Test
    void testHikariCP() {
        final var hikariConfig = createHikariConfig();
        hikariConfig.setMaximumPoolSize(5); // 커넥션 풀 최대 크기
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true"); // PreparedStatement 캐시 활성화
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250"); // PreparedStatement 캐시 크기
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048"); // 캐시할 PreparedStatement의 최대 크기

        final var dataSource = new HikariDataSource(hikariConfig);
        final var properties = dataSource.getDataSourceProperties();

        assertThat(dataSource.getMaximumPoolSize()).isEqualTo(5);
        assertThat(properties.getProperty("cachePrepStmts")).isEqualTo("true");
        assertThat(properties.getProperty("prepStmtCacheSize")).isEqualTo("250");
        assertThat(properties.getProperty("prepStmtCacheSqlLimit")).isEqualTo("2048");

        dataSource.close();
    }

    @Test
    void testPreparedStatementCaching() throws SQLException {
        final var hikariConfig = createHikariConfig();
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        final var dataSource = new HikariDataSource(hikariConfig);

        long startTime, endTime;

        // 비캐시된 PreparedStatement 실행
        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT 1"; // 간단한 쿼리

            // 첫 번째 실행 (비캐시)
            startTime = System.nanoTime();
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.executeQuery();
            }
            endTime = System.nanoTime();
            long nonCachedDuration = endTime - startTime;
            // Non-cached PreparedStatement duration: 23324800 ns
            System.out.println("Non-cached PreparedStatement duration: " + nonCachedDuration + " ns");
        }

        // 캐시된 PreparedStatement 실행
        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT 1"; // 동일한 쿼리

            // 두 번째 실행 (캐시)
            startTime = System.nanoTime();
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.executeQuery();
            }
            endTime = System.nanoTime();
            long cachedDuration = endTime - startTime;
            // Cached PreparedStatement duration: 406500 ns
            System.out.println("Cached PreparedStatement duration: " + cachedDuration + " ns");
        }

        dataSource.close();
    }

    private static HikariConfig createHikariConfig() {
        final var hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(H2_URL);
        hikariConfig.setUsername(USER);
        hikariConfig.setPassword(PASSWORD);

        return hikariConfig;
    }
}
