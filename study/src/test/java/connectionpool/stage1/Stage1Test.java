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
     * 커넥션 풀링(Connection Pooling)이란? DataSource 객체를 통해 미리 커넥션(Connection)을 만들어 두는 것을 의미한다. 새로운 커넥션을 생성하는 것은 많은 비용이 들기에
     * 미리 커넥션을 만들어두면 성능상 이점이 있다. 커넥션 풀링에 미리 만들어둔 커넥션은 재사용 가능하다.
     * <p>
     * h2에서 제공하는 JdbcConnectionPool를 다뤄보며 커넥션 풀에 대한 감을 잡아보자.
     * <p>
     * Connection Pooling and Statement Pooling
     * https://docs.oracle.com/en/java/javase/11/docs/api/java.sql/javax/sql/package-summary.html 커넥션 풀링에 대한 개념은 이미 널리
     * 알려져있기 때문에 자세한 서술은 생략하고, Statement 풀링에 대해 좀 더 알아보았다. 일반적으로 데이터베이스가 SQL을 실행하기 위해선 여러 단계를 거친다. 그 단계 중 SQL의 구문을 분석해
     * 쿼리를 어떻게 실행할 것인지 판단하는 과정이 있다. Statement 풀링이란, 같은 SQL을 매번 분석할 필요없이, 이전에 분석한 것을 재사용하는 아이디어다. 원칙상 모든 DBMS에서 이를 지원하지는
     * 않는다. JDBC에서 이를 위한 API로 PreparedStatement를 제공한다. Mysql에서 이를 사용하기 위해서는 useServerPrepStmts 옵션을 활성화해야 한다. 그렇지 않은 상태에서
     * PreparedStatement를 사용하면 이점을 누릴 수 없다.
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
     * HikariCP의 pool size는 몇으로 설정하는게 좋을까? https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing 데이터베이스
     * 입장에서 커넥션을 제공하려면, 데이터베이스가 사용하는 스레드가 증가된다. 따라서, 커넥션이 많아지면 데이터베이스가 각 커넥션을 처리하기 위해 스레드간의 컨텍스트 스위칭이 발생한다. 또한, 데이터베이스의
     * 작업은 IO 작업이 차지하는 부분이 크기 때문에 스레드가 많아질 때 단점이 커진다. IO 작업을 하는 도중에는 그 스레드를 처리할 수 없기 때문에 컨텍스트 스위칭이 발생하고, 이것이 자주 발생하는
     * 환경이기 때문. 따라서, 데이터베이스의 성능을 최대한 뽑아내려면 데이터베이스 서버의 코어 수에 비해 너무 많으면 성능이 저하된다. IO가 많은 작업이라 해도 CPU 사용을 하지 않는 것은 아니기 때문에,
     * CPU 코어 수와 동일할 필요는 없다. HikariCP 에서는 CPU 코어 수에 2배를 곱하고 유효 스핀들 개수를 더해준다. 유효 스핀들 개수는 하드 디스크의 스핀들 개수다.
     * <p>
     * HikariCP를 사용할 때 적용하면 좋은 MySQL 설정 https://github.com/brettwooldridge/HikariCP/wiki/MySQL-Configuration
     * <p>
     * 위 링크의 내용은 모두 PreparedStatement 와 관련된 설정이다. 현대 웹 프로그래밍에서 사용되는 쿼리의 특성을 고려해 추천 값을 제시해준다.
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
