package nextstep.jdbc.testUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestDatabaseUtils {

    private static final Logger log = LoggerFactory.getLogger(TestDatabaseUtils.class);

    public static void execute(final DataSource dataSource) {
        try (final Connection connection = dataSource.getConnection();
             final Statement statement = connection.createStatement();
        ) {
            final var url = TestDatabaseUtils.class.getClassLoader().getResource("schema.sql");
            final var file = new File(Objects.requireNonNull(url).getFile());
            final var sql = Files.readString(file.toPath());
            statement.execute(sql);
        } catch (NullPointerException | IOException | SQLException e) {
            log.error(e.getMessage(), e);
        }
    }

    private TestDatabaseUtils() {
    }
}
