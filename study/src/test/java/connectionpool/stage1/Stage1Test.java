package connectionpool.stage1;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.jupiter.api.Test;

import javax.sql.ConnectionEvent;
import javax.sql.PooledConnection;
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
        // Connection Pool - DataSource 를 가지고 있음
        final JdbcConnectionPool jdbcConnectionPool = JdbcConnectionPool.create(H2_URL, USER, PASSWORD);

        assertThat(jdbcConnectionPool.getActiveConnections()).isZero();

        //    private final Queue<PooledConnection> recycledConnections = new ConcurrentLinkedQueue<>();
        // Queue 에서 pool 을 통해 하나를 뺌
        // ActiveConnection 개수를 판단해서 감지
        try (final var connection = jdbcConnectionPool.getConnection()) {
            assertThat(connection.isValid(1)).isTrue();
            assertThat(jdbcConnectionPool.getActiveConnections()).isEqualTo(1);
        }

        // connection 이 닫힐때 재활용 함수 호출
//        public void connectionClosed(ConnectionEvent event) {
//            PooledConnection pc = (PooledConnection) event.getSource();
//            pc.removeConnectionEventListener(this);
//            recycleConnection(pc);
//        }

        assertThat(jdbcConnectionPool.getActiveConnections()).isZero();

        jdbcConnectionPool.dispose();
    }

    /**
     * Spring Boot 2.0 부터 HikariCP를 기본 데이터 소스로 채택하고 있다.
     * https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#data.sql.datasource.connection-pool
     * Supported Connection Pools
     * We prefer HikariCP for its performance and concurrency. If HikariCP is available, we always choose it.
     *
     * HikariCP 공식 문서를 참고하여 HikariCP를 설정해보자.
     * https://github.com/brettwooldridge/HikariCP#rocket-initialization
     *
     * HikariCP 필수 설정
     * https://github.com/brettwooldridge/HikariCP#essentials
     *
     * HikariCP의 pool size는 몇으로 설정하는게 좋을까?
     * https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing
     *
     * HikariCP를 사용할 때 적용하면 좋은 MySQL 설정
     * https://github.com/brettwooldridge/HikariCP/wiki/MySQL-Configuration
     */
    @Test
    void testHikariCP() {
        final var hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(H2_URL);
        hikariConfig.setUsername(USER);
        hikariConfig.setPassword(PASSWORD);
        hikariConfig.setMaximumPoolSize(5);
        // PrepareStatement Cache 할지
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        // Cache 할 PrepareStatement SQL 문 개수
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        // PrepareStatement 길이가 2048(문자수) 이하일때만 허용
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        // connection-time : 기본 30초 , 최소 0.25초
        // idle-time : 커넥션 풀에서 유휴 상태로 대기할 수 있는 시간, 기본 10분 (600,000ms), 0으로 설정하면 유휴 상태에서도 연결이 제거 X
        // keep-alive-time : DB 에 요청을 보내 커넥션을 살리는 주기 시간, 0 (사용 안 함), 30초 (30,000ms), 권장 범위는 분 단위
        //  maxLifetime : 연결이 풀에서 사용 가능한 최대 시간, 기본값: 30분 (1,800,000ms), 최소 허용값: 30초 (30,000ms)
        final var dataSource = new HikariDataSource(hikariConfig);
        final var properties = dataSource.getDataSourceProperties();

        assertThat(dataSource.getMaximumPoolSize()).isEqualTo(5);
        assertThat(properties.getProperty("cachePrepStmts")).isEqualTo("true");
        assertThat(properties.getProperty("prepStmtCacheSize")).isEqualTo("250");
        assertThat(properties.getProperty("prepStmtCacheSqlLimit")).isEqualTo("2048");

        dataSource.close();
    }
}
