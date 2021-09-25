package com.techcourse.support.jdbc.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabasePopulatorUtils {

    private static final Logger LOG = LoggerFactory.getLogger(DatabasePopulatorUtils.class);

    private DatabasePopulatorUtils() {}

    public static void execute(DataSource dataSource) {
        try {
            final URL url = DatabasePopulatorUtils.class.getClassLoader().getResource("schema.sql");
            final File file = new File(url.getFile());
            final String sql = Files.readString(file.toPath());

            executeSql(dataSource, sql);
        } catch (NullPointerException | IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private static void executeSql(DataSource dataSource, String sql) {
        try (
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement()
        ){
            statement.execute(sql);
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
