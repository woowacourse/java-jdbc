package com.techcourse.support.jdbc.init;

import java.net.URL;
import nextstep.jdbc.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class DatabasePopulatorUtils {

    private static final Logger log = LoggerFactory.getLogger(DatabasePopulatorUtils.class);

    private DatabasePopulatorUtils() {}

    public static void execute(final DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        try {
            var url = DatabasePopulatorUtils.class.getClassLoader().getResource("schema.sql");
            validateUrl(url);
            var file = new File(url.getFile());
            var sql = Files.readString(file.toPath());

            jdbcTemplate.update(sql);
        } catch (IOException e) {
            throw new RuntimeException("I/O 예외가 발생했습니다");
        }
    }

    private static void validateUrl(final URL url) {
        if (url == null) {
            throw new RuntimeException("해당 경로에 파일이 존재하지 않습니다.");
        }
    }
}
