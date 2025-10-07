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
     * https://docs.oracle.com/en/java/javase/11/docs/api/java.sql/javax/sql/package-summary.html
     */
    @Test
    void testJdbcConnectionPool() throws SQLException {
        // 커넥션풀 생성
        final JdbcConnectionPool jdbcConnectionPool =
                JdbcConnectionPool.create(H2_URL, USER, PASSWORD);

        // (초기에는 커넥션풀에 활성 커넥션이 0개인 것을 확인)
        assertThat(jdbcConnectionPool.getActiveConnections()).isZero();

        // 커넥션풀에 커넥션 가져온다.
        try (final var connection = jdbcConnectionPool.getConnection()) {
            // 가져온 커넥션이 유효한지 확인 (1초 타임아웃)
            assertThat(connection.isValid(1)).isTrue();

            // 커넥션풀의 활성 커넥션수가 1개인 것을 확인
            assertThat(jdbcConnectionPool.getActiveConnections()).isEqualTo(1);

            // try-with-resources 구문을 통해 커넥션이 자동으로 닫히고 풀로 반환됨
        }

        // (커넥션을 반납한 이후에는 활성 커넥션 수가 다시 0개인 것을 확인)
        assertThat(jdbcConnectionPool.getActiveConnections()).isZero();

        // 커넥션풀 리소스 해제
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
     * HikariCP의 pool size는 몇으로 설정하는게 좋을까? https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing
     * <p>
     * HikariCP를 사용할 때 적용하면 좋은 MySQL 설정 https://github.com/brettwooldridge/HikariCP/wiki/MySQL-Configuration
     */
    @Test
    void testHikariCP() {
        final var hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(H2_URL);
        hikariConfig.setUsername(USER);
        hikariConfig.setPassword(PASSWORD);
        hikariConfig.setMaximumPoolSize(5);
        // cachePrepStmts : Prepared Statement 캐시 기능 자체를 활성화할지 여부
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        // prepStmtCacheSize  : 커넥션별로 MySQL 드라이버가 캐시할 Prepared Statement 개수
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        // prepStmtCacheSqlLimit : 캐시할 Prepared Statement의 최대 SQL 문 길이(문자 수)
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
