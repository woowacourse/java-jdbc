package nextstep.jdbc.support;

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
import java.util.Objects;

public class DatabasePopulatorUtils {

    private static final Logger log = LoggerFactory.getLogger(DatabasePopulatorUtils.class);

    public static void execute(DataSource dataSource, String sqlFileName) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            final URL url = DatabasePopulatorUtils.class.getClassLoader().getResource(sqlFileName);
            log.info("sql file read {}", sqlFileName);
            final File file = new File(Objects.requireNonNull(url, "sql 파일이 존재하지않습니다.").getFile());
            final String sql = Files.readString(file.toPath());
            statement.execute(sql);
        } catch (NullPointerException | IOException | SQLException e) {
            log.error(e.getMessage(), e);
        }
    }

    private DatabasePopulatorUtils() {}
}
