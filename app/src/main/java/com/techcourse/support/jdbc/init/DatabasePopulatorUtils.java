package com.techcourse.support.jdbc.init;

import com.techcourse.exception.FileReadFailException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabasePopulatorUtils {

    private static final Logger log = LoggerFactory.getLogger(DatabasePopulatorUtils.class);

    public static void execute(final DataSource dataSource) {
        String sql = readStringFile();
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
    }

    private static String readStringFile() {
        try {
            final var url = DatabasePopulatorUtils.class.getClassLoader().getResource("schema.sql");
            final var file = new File(url.getFile());
            return Files.readString(file.toPath());
        } catch (NullPointerException | IOException e) {
            throw new FileReadFailException("파일 읽는 과정에서 예외가 발생하였습니다.", e);
        }
    }

    private DatabasePopulatorUtils() {
    }
}
