package nextstep.datasource;

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

public class DatabasePopulator {

    private static final Logger log = LoggerFactory.getLogger(DatabasePopulator.class);

    private final DataSource dataSource;

    public DatabasePopulator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(URL url) {
        try (
                final Connection connection = dataSource.getConnection();
                final Statement statement = connection.createStatement()
        ) {
            String sql = readSql(url);
            log.debug("query : {}", sql);
            statement.execute(sql);
        } catch (NullPointerException | IOException | SQLException e) {
            log.error(e.getMessage(), e);
        }
    }

    private String readSql(URL url) throws IOException {
        File file = new File(url.getFile());
        return Files.readString(file.toPath());
    }
}
