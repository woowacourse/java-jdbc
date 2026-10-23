package com.techcourse.support.jdbc.init;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabasePopulatorUtils {

    public static void execute(final DataSource dataSource) {
        final var url = DatabasePopulatorUtils.class.getClassLoader().getResource("schema.sql");

        if (url == null) {
            throw new IllegalStateException("schema.sql 파일을 찾을 수 없습니다.");
        }

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            final var sql = Files.readString(new File(url.getFile()).toPath());
            statement.execute(sql);
        } catch (IOException | SQLException e) {
            throw new IllegalStateException("스키마 초기화에 실패했습니다.", e);
        }
    }

    private DatabasePopulatorUtils() {}
}
