package com.techcourse.support.jdbc.init;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javax.sql.DataSource;
import nextstep.jdbc.core.JdbcTemplate;

public class DatabasePopulatorUtils {

    private DatabasePopulatorUtils() {
    }

    public static void execute(final DataSource dataSource) {
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        final var url = DatabasePopulatorUtils.class.getClassLoader().getResource("schema.sql");
        if (url == null) {
            throw new RuntimeException("해당 resource가 존재하지 않습니다.");
        }
        final var file = new File(url.getFile());
        try {
            final var sql = Files.readString(file.toPath());
            jdbcTemplate.execute(sql);
        } catch (IOException e) {
            throw new RuntimeException("입출력 예외가 발생했습니다.");
        }
    }
}
