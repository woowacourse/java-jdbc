package com.techcourse.support.jdbc.init;

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

    private DatabasePopulatorUtils() {
    }

    public static void execute(final DataSource dataSource) {
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        final var url = DatabasePopulatorUtils.class.getClassLoader().getResource("schema.sql");
        validateUrlExists(url);

        final var file = new File(url.getFile());
        try {
            final var sql = Files.readString(file.toPath());
            jdbcTemplate.execute(sql);
        } catch (IOException e) {
            throw new RuntimeException("입출력 예외가 발생했습니다.");
        }
    }

    private static void validateUrlExists(final URL url) {
        if (url == null) {
            throw new ResourceNotFoundException();
        }
    }
}
