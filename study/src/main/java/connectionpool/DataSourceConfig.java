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


    // HikariCP를 위한 데이터소스를 빈으로 등록
    @Bean
    public DataSource hikariDataSource() {
        // HikariCP 설정 객체 생성
        final var hikariConfig = new HikariConfig();

        // 커넥션풀의 이름 지정 (로그나 모니터링에서 커넥션풀을 식별하기 위해)
        hikariConfig.setPoolName("gugu");

        // 기본적인 DB 연결 정보 세팅
        hikariConfig.setJdbcUrl(H2_URL);
        hikariConfig.setUsername(USER);
        hikariConfig.setPassword(PASSWORD);

        // 최대 풀사이즈는 지정 (현재는 5개)
        hikariConfig.setMaximumPoolSize(MAXIMUM_POOL_SIZE);
        // 커넥션이 유효한지 확인할 간단한 쿼리 설정
        hikariConfig.setConnectionTestQuery("VALUES 1");

        // cachePrepStmts : Prepared Statement 캐시 기능 자체를 활성화할지 여부
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        // prepStmtCacheSize  : 커넥션별로 MySQL 드라이버가 캐시할 Prepared Statement 개수
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        // prepStmtCacheSqlLimit : 캐시할 Prepared Statement의 최대 SQL 문 길이(문자 수)
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        // Connection Leak Detection 설정
        hikariConfig.setLeakDetectionThreshold(2000);

        return new HikariDataSource(hikariConfig);
    }
}
