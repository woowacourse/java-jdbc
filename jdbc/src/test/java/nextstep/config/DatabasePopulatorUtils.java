package nextstep.config;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import javax.sql.DataSource;

public class DatabasePopulatorUtils {

    private DatabasePopulatorUtils() {
    }

    public static void execute(DataSource dataSource) {

        try (Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement()) {

            final String sql = getSql();

            statement.execute(sql);
        } catch (NullPointerException | SQLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static String getSql() {
        try {
            final URL url = DatabasePopulatorUtils.class.getClassLoader().getResource("schema.sql");
            final File file = new File(Objects.requireNonNull(url).getFile());
            return Files.readString(file.toPath());
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
