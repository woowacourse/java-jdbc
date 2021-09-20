package com.techcourse.support.jdbc.init;

import com.techcourse.exception.FileReadException;
import com.techcourse.exception.SqlInitException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabasePopulatorUtils {

    private static final Logger LOG = LoggerFactory.getLogger(DatabasePopulatorUtils.class);

    private DatabasePopulatorUtils() {
    }

    public static void execute(DataSource dataSource) {

        try (Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement()) {

            final String sql = getSql();

            statement.execute(sql);
        } catch (NullPointerException | SQLException e) {
            throw new SqlInitException(e);
        }
    }

    private static String getSql() {
        try {
            final URL url = DatabasePopulatorUtils.class.getClassLoader().getResource("schema.sql");
            final File file = new File(Objects.requireNonNull(url).getFile());
            return Files.readString(file.toPath());
        } catch (IOException e) {
            throw new FileReadException(e);
        }
    }
}
