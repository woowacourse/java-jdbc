package com.techcourse.support.jdbc.init;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

import nextstep.jdbc.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabasePopulatorUtils {

    private static final Logger log = LoggerFactory.getLogger(DatabasePopulatorUtils.class);

    public static void execute(JdbcTemplate jdbcTemplate) {
        try {
            final URL url = DatabasePopulatorUtils.class.getClassLoader().getResource("schema.sql");
            final File file = new File(url.getFile());
            final String sql = Files.readString(file.toPath());
            jdbcTemplate.update(sql);
        } catch (NullPointerException | IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private DatabasePopulatorUtils() {}
}
