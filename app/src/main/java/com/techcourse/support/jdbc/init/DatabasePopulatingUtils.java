package com.techcourse.support.jdbc.init;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.Statement;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabasePopulatingUtils {

    private static final Logger LOG = LoggerFactory.getLogger(DatabasePopulatingUtils.class);

    private DatabasePopulatingUtils() {
    }

    public static void execute(DataSource dataSource) {
        try (
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement()
        ) {
            final URL url = DatabasePopulatingUtils.class
                .getClassLoader()
                .getResource("schema.sql");
            final File file = new File(url.getFile());
            final String sql = Files.readString(file.toPath());

            statement.execute(sql);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
