package nextstep.jdbc.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import javax.sql.DataSource;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.executor.QueryExecuteResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabasePopulatorUtils {

    private static final Logger log = LoggerFactory.getLogger(DatabasePopulatorUtils.class);
    private static final String DEFAULT_SCHEMA_FILE_NAME = "schema.sql";

    public static void execute(DataSource dataSource) {
        execute(dataSource, DEFAULT_SCHEMA_FILE_NAME);
    }

    public static void execute(DataSource dataSource, String fileName) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        try {
            final URL url = DatabasePopulatorUtils.class.getClassLoader().getResource(fileName);
            final File file = new File(url.getFile());
            final String sql = Files.readString(file.toPath());
            QueryExecuteResult queryExecuteResult = jdbcTemplate.executeDDL(sql);
            log.info("영향받은 row 수 : {}", queryExecuteResult.effectedRow());
        } catch (NullPointerException | IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private DatabasePopulatorUtils() {
    }
}
