package com.techcourse.support.jdbc.init;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javax.sql.DataSource;
import nextstep.jdbc.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabasePopulatorUtils {

    private static final Logger log = LoggerFactory.getLogger(DatabasePopulatorUtils.class);

    public static void execute(final DataSource dataSource) {
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        final var url = DatabasePopulatorUtils.class.getClassLoader().getResource("schema.sql");
        final var file = new File(url.getFile());
        try {
            String sql = Files.readString(file.toPath());
            jdbcTemplate.update(sql);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
