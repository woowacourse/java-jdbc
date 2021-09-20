package com.techcourse.support.jdbc.init;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;

import nextstep.jdbc.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabasePopulatorUtils {

    private static final Logger log = LoggerFactory.getLogger(DatabasePopulatorUtils.class);

    public static void execute(DataSource dataSource) {
        try (Connection conn = dataSource.getConnection();
             Statement statement = conn.createStatement()) {
            final String sql = getInitSchema();
            statement.execute(sql);
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        } catch (URISyntaxException | IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private static String getInitSchema() throws URISyntaxException, IOException {
        final URL url = DatabasePopulatorUtils.class.getClassLoader().getResource("schema.sql");
        final File file = Paths.get(url.toURI()).toFile();
        return Files.readString(file.toPath());
    }

    private DatabasePopulatorUtils() {}
}
