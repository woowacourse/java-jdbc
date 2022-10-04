package nextstep.jdbc.support;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabasePopulatorUtils {

    private static final Logger log = LoggerFactory.getLogger(DatabasePopulatorUtils.class);

    public static void init(final DataSource dataSource) {
        executeSQL(dataSource, "schema.sql");
    }

    public static void clear(final DataSource dataSource) {
        executeSQL(dataSource, "clear.sql");
    }

    private static void executeSQL(final DataSource dataSource, final String fileName) {
        try (
            final Connection connection = dataSource.getConnection();
            final Statement statement = connection.createStatement()
        ) {
            final var url = DatabasePopulatorUtils.class.getClassLoader().getResource(fileName);
            final var file = new File(url.getFile());
            final var sql = Files.readString(file.toPath());

            statement.execute(sql);
        } catch (NullPointerException | IOException | SQLException e) {
            log.error(e.getMessage(), e);
        }
    }

    private DatabasePopulatorUtils() {}
}
