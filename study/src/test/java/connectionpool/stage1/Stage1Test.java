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
     */
    @Test
    void testJdbcConnectionPool() throws SQLException {
        // default connection time : 30 초
        // default max connections : 10 개
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
     *
     풀 사이즈 : https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing
     Once the number of threads exceeds the number of CPU cores, you're going slower by adding more threads, not faster.
     쓰레드 개수가 CPU 코어를 초과한다면 thread가 추가될 때마다 계속 성능이 떨어질 것이다.
     ConnectionPool의 개수를 결정짓는 4가지 지표 : CPU, DISK NETWORK and MEMORY
     DISK I/O에 block이 걸리면 thread/connection/query도 block이 걸린다. - thread가 block이 되기 때문에 cpu core보다 많은 쓰레드가 실행될 수 잇는 것이다.
     SSD로 바뀌면 I/O block이 줄고 결국엔 thread의 block하는 시간이 줄어드므로 thread pool의 개수가 적은게 유리하다?
     최적의 connection count는 아래의 공식에서 시작하는게 좋다고 한다.

     ```
     connection_count = ((corecount * 2) + spindle_count)
     ```
     spindle_count라는건 disk를 raid를 얼마나 구성했는지에 대한 말인 것 같다.

     만약 메모리에 모든 데이터를 캐싱을 했다면 spindle_count는 0으로 설정해도 된다고 할 수 있을 정도의 값이라고 한다.
     cpu가 좋아서 virtual thread를 지원하는데 걱정 ㄴㄴ 물리적인 core-count만 생각해보자.
     그럼 ec2 cpu와 disk들은 다 가상 cpu인데 어떻게 connection poll 값을 설정해야 할지도 구민해봐야 함.
     참고 : https://dba.stackexchange.com/questions/228663/what-is-effective-spindle-count

     여러 thread에서 connection pool에 동시 접근하면 발생하는 pool-locking도 고려해야 하는데,
     동시에 connection을 요청하는 평균 값 고려 필요
     ```
     (thread 개수 - thread pool로 제어할 수 있지) x ((동시 요청 수) - 1) + 1
     ```
     *
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

        // preparedstatement를 재사용하기 위한 설정.
        // 해당 설정은 hikaripool이 아닌 각 jdbc 구현제에서 활용된다 (MySQL : https://dev.mysql.com/doc/connector-j/en/connector-j-connp-props-performance-extensions.html")
        // mysql에서는 preparedstatement를 캐싱하여 성능을 개선한다고 한다. :https://dev.mysql.com/doc/refman/8.0/en/statement-caching.html
        // This cache helps reduce the overhead associated with repeatedly parsing and checking queries.
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");

        // https://github.com/brettwooldridge/HikariCP/wiki/MySQL-Configuration 에서 권장하는 최적 값들
        // cachesize는 preparedStatement의 개수를 얼마나 캐싱할 것임을 뜻
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        // sqlLimit은 sql 문자열 길이 제한. 캐시 길이보다 넘는 sql을 실행하면 캐시되지 않는다고 한다.
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
