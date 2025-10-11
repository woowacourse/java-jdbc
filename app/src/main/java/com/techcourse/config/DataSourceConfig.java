package com.techcourse.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;

public class DataSourceConfig {
    private static final String PROPERTIES_FILE = "application.properties";

    private static final String URL_PROPERTY = "datasource.url";
    private static final String USERNAME_PROPERTY = "datasource.username";
    private static final String PASSWORD_PROPERTY = "datasource.password";

    private static DataSource INSTANCE;

    private DataSourceConfig() {
    }

    public static DataSource getInstance() {
        if (Objects.isNull(INSTANCE)) {
            INSTANCE = createJdbcDataSource();
        }
        return INSTANCE;
    }

    private static JdbcDataSource createJdbcDataSource() {
        Properties properties = loadProperties();

        final var jdbcDataSource = new JdbcDataSource();
        jdbcDataSource.setUrl(getRequiredProperty(properties, URL_PROPERTY));
        jdbcDataSource.setUser(getRequiredProperty(properties, USERNAME_PROPERTY));
        jdbcDataSource.setPassword(getRequiredProperty(properties, PASSWORD_PROPERTY));
        return jdbcDataSource;
    }

    private static Properties loadProperties() {
        try (InputStream input = DataSourceConfig.class.getResourceAsStream("/" + PROPERTIES_FILE)) {
            if (input == null) {
                throw new ConfigurationException(PROPERTIES_FILE + " not found in classpath");
            }

            Properties properties = new Properties();
            properties.load(input);
            return properties;
        } catch (IOException e) {
            throw new ConfigurationException("Failed to load properties from " + PROPERTIES_FILE, e);
        }
    }

    private static String getRequiredProperty(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new ConfigurationException("Missing required property: " + key);
        }
        return value;
    }
}
