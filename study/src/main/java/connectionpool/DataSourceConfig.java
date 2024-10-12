package connectionpool;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSourceConfig {

    public static final int MAXIMUM_POOL_SIZE = 5;
    private static final String H2_URL = "jdbc:h2:./test;DB_CLOSE_DELAY=-1";
    private static final String USER = "sa";
    private static final String PASSWORD = "";


    @Bean
    public DataSource hikariDataSource() {
        final var hikariConfig = new HikariConfig();
        hikariConfig.setPoolName("gugu");
        hikariConfig.setJdbcUrl(H2_URL);
        hikariConfig.setUsername(USER);
        hikariConfig.setPassword(PASSWORD);
        hikariConfig.setMaximumPoolSize(MAXIMUM_POOL_SIZE);
        hikariConfig.setConnectionTestQuery("VALUES 1");

        /*
         * HikariCP 공식 문서를 보면 HikariCP는 PreparedStatement 캐싱 기능을 제공하지 않는다고 쓰여 있다.
         * https://github.com/brettwooldridge/HikariCP#statement-cache
         * > Many connection pools, including Apache DBCP, Vibur, c3p0 and others offer PreparedStatement caching. HikariCP does not. Why?
         *
         * 정확하겐 HikariCP 자체는 PreparedStatement 캐싱을 지원하지 않는 대신, 사용중인 JdBC Driver에 캐싱 작업을 위임한다.
         * HikariCP는 단순히 Connection Pool일 뿐이며, 그 외의 기능들은 사용중인 JDBC Driver에 위임한다.
         *
         * HikariCP 공식 문서를 더 읽어보면 그 이유에 대해 아래와 같이 설명한다.
         * Connection Pool 레벨에서 PreparedStatement는 각 Connection 별로 캐싱될 수 밖에 없다.
         * 만일 애플리케이션에서 250개의 자주 실행되는 쿼리가 있고, 커넥션 풀이 20개의 커넥션을 가지고 있다면
         * 데이터베이스는 5000개의 쿼리 실행 계획을 보관해야 하며, 마찬가지로 커넥션 풀도 이 많은 PreparedStatement를 캐싱해야 한다.
         *
         * 이와 달리 JDBC Driver 레벨에선 PreparedStatement를 효율적으로 캐싱할 수 있다.
         * 대부분의 주요 데이터베이스 JDBC Driver 는 이미 PreparedStatement 캐시를 설정할 수 있는 기능을 가지고 있다.
         * JDBC Driver는 커넥션 간에 실행 계획을 공유할 수 있기에 자주 실행되는 250개의 쿼리에 대해 250개의 실행 계획만 보관할 수 있게 한다.
         *
         * https://stackoverflow.com/questions/71493599/does-hikari-cp-support-preparedstatements-cache
         * */
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        return new HikariDataSource(hikariConfig);
    }
}
