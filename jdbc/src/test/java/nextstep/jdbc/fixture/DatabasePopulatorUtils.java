package nextstep.jdbc.fixture;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabasePopulatorUtils {

    private static final Logger log = LoggerFactory.getLogger(DatabasePopulatorUtils.class);

    public static void execute(DataSource dataSource) {
        try (Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement()) {
            final URL url = DatabasePopulatorUtils.class.getClassLoader().getResource("schema.sql");
            final File file = new File(url.getFile());
            final String sql = Files.readString(file.toPath());
            stmt.execute(sql);
        } catch (NullPointerException | IOException | SQLException e) {
            log.error(e.getMessage(), e);
        }
    }

    private DatabasePopulatorUtils() {}
}
