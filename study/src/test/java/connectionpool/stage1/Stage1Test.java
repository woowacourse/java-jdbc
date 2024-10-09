package connectionpool.stage1;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
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
     * -> CPU 코어만 고려했을 때 pool size는 cpu 코어 수와 동일하면 최적
     * 하지만 실제로는 디스크, 네트워크하는 I/O가 있기 때문에 해당 I/O 시간동안 cpu에서 실행되는 스레드는 대기
     * 따라서 컨텍스트 스위칭을 통해 작업하고 있는 스레드 대신 다른 스레드도 커넥션을 할당받아 작업할 수 있음
     * PostgreSql에 서 제안한 커넥션 사이즈 : connections = (corecount * 2) + effectivespindle_count
     * 코어 뿐 아니라 디스크, 네트워크도 고려했을 때 코어 개수에 2배를 하는 것을 추천
     * 하드디스크가 관리할 수 있는 동시 I/O 요청수를 spindle이라고 칭함, 디스크가 n개 있는 경우
     * effective_spindle_count는 n
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
        // 커넥션 풀이 가질 수 있는 최대 커넥션 사이즈, 사용중인 커넥션과 유휴인 커넥션을 몸두 포함, 기본값 10
        hikariConfig.setMaximumPoolSize(5);
        //PreparedStatement에 대한 캐싱 활성화 옵션, 기본값 false
        //동일하게 반복되는 sql에 대해서 캐싱된 PreparedStatement를 사용하여 성능 최적화 가능
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        //캐싱할 PreparedStatement의 최대 개수, 기본값 25이지만 권장값 250~500
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        //캐싱할 SQL문의 최대 길이, 기본값 256이지만 권장값 2048
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
