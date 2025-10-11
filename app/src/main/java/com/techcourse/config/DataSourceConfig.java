package com.techcourse.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;
import org.h2.jdbcx.JdbcDataSource;

public class DataSourceConfig {

    private static final String PROPERTIES_FILE = "/application.properties";
    private static final String DATASOURCE_URL_PROPERTY = "datasource.url";
    private static final String DATASOURCE_USERNAME_PROPERTY = "datasource.username";
    private static final String DATASOURCE_PASSWORD_PROPERTY = "datasource.password";

    private static javax.sql.DataSource INSTANCE;

    public static javax.sql.DataSource getInstance() {
        if (Objects.isNull(INSTANCE)) {
            INSTANCE = createJdbcDataSource();
        }
        return INSTANCE;
    }

    private static JdbcDataSource createJdbcDataSource() {
        try (final InputStream inputStream = DataSourceConfig.class.getResourceAsStream(PROPERTIES_FILE)) {
            if (inputStream == null) {
                throw new ConfigurationException("application.properties not found in classpath");
            }
            final Properties properties = new Properties();
            properties.load(inputStream);

            final var jdbcDataSource = new JdbcDataSource();
            jdbcDataSource.setUrl(properties.getProperty(DATASOURCE_URL_PROPERTY));
            jdbcDataSource.setUser(properties.getProperty(DATASOURCE_USERNAME_PROPERTY));
            jdbcDataSource.setPassword(properties.getProperty(DATASOURCE_PASSWORD_PROPERTY));
            return jdbcDataSource;
        } catch (final IOException e) {
            throw new ConfigurationException("Failed to load application.properties");
        }
    }

    private DataSourceConfig() {
    }
}
