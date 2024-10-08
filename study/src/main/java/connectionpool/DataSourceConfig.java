package connectionpool;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSourceConfig {
    DataSourceProperty dataSourceProperty;

    public DataSourceConfig(DataSourceProperty dataSourceProperty) {
        this.dataSourceProperty = dataSourceProperty;
    }

    @Bean
    public DataSource hikariDataSource() {
        final var hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(dataSourceProperty.getJdbcUrl());
        hikariConfig.setUsername(dataSourceProperty.getUserName());
        hikariConfig.setPassword(dataSourceProperty.getPassword());
        hikariConfig.setPoolName(dataSourceProperty.getPoolName());
        hikariConfig.setMaximumPoolSize(dataSourceProperty.getMaximumPoolSize());
        hikariConfig.setConnectionTestQuery("VALUES 1");
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        return new HikariDataSource(hikariConfig);
    }
}
