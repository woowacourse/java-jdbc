package connectionpool.stage1;

import static org.assertj.core.api.Assertions.assertThat;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.SQLException;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.jupiter.api.Test;

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
     *
     * 커넥션 풀
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
     *
     *  pool size = Tn x (Cm - 1) + 1
     *  Tn : 최대 스레드 수
     *  Cm : 단일 스레드가 보유할 수 있는 최대 동시 연결 수
     *  만약에 쓰레드가 4대임
     *  특정 작업을 하는데 최대 3개의 커넥션이 필요함
     *  커넥션 풀에 12개의 커넥션이 있으면 좋지만 최소로 유지하려고 할때
     *  만약 풀에 8개의 커넥션이 있다면? -> 각 쓰레드가 커넥션을 2개씩 점유해버리면 데드락에 걸려버림
     *  만약 풀에 9개의 커넥션이 있다면? -> 각 쓰레드가 커넥션을 2개씩 점유해도 한 쓰레드는 남은 커넥션 하나를 사용할 수 있어 작업을 끝내고 커넥션을 반납함
     *
     * prepStmtCacheSize : MySQL 드라이버가 connection 당 캐시할 prepStmt 수를 설정합니다. 기본값은 보수적인 25개입니다. 250-500 사이로 설정하는 것이 좋습니다.
     * prepStmtCacheSqlLimit : 드라이버가 캐시할 준비된 SQL 문의 최대 길이입니다. MySQL 기본값은 256입니다.
     *                         경험상, 특히 Hibernate와 같은 ORM 프레임워크의 경우 이 기본값은 생성된 문 길이의 임계값보다 훨씬 낮습니다. 권장 설정은 2048입니다.
     * cachePrepStmts : 캐시가 기본적으로 비활성화되어 있는 경우 위의 두 매개변수는 아무런 영향을 미치지 않습니다. 이 매개 변수를 true로 설정해야 합니다.
     * useServerPrepStmts : 최신 버전의 MySQL은 서버 측 준비 문을 지원하므로 성능이 크게 향상될 수 있습니다. 이 속성을 true로 설정합니다.
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
}
