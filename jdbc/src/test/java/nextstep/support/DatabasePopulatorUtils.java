package nextstep.support;

import nextstep.jdbc.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

public class DatabasePopulatorUtils {

    private static final Logger log = LoggerFactory.getLogger(DatabasePopulatorUtils.class);

    private DatabasePopulatorUtils() {}

    public static void execute(DataSource dataSource) {
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        try {
            final URL url = DatabasePopulatorUtils.class.getClassLoader().getResource("schema.sql");
            String sql = Files.readString(new File(url.getFile()).toPath());
            jdbcTemplate.update(sql);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
