package com.techcourse.support.jdbc.init;

import com.techcourse.support.jdbc.init.exception.ExecuteSqlException;
import com.techcourse.support.jdbc.init.exception.FileReadException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabasePopulatorUtils {

    private static final Logger LOG = LoggerFactory.getLogger(DatabasePopulatorUtils.class);
    private static final String SCHEMA_SQL_EXECUTE_EXCEPTION_MESSAGE = "Schema SQL파일 실행에 실패했습니다.";
    private static final String SCHEMA_SQL_FILE_READ_EXCEPTION_MESSAGE = "Schema SQL파일을 읽어오는데에 실패했습니다.";

    private DatabasePopulatorUtils() {
    }

    public static void execute(DataSource dataSource) {
        final String sql = readSchemaSqlFileByPath("schema.sql");
        executeSql(dataSource, sql);
    }

    private static void executeSql(DataSource dataSource, String sql) {
        try (final Connection connection = dataSource.getConnection();
            final Statement statement = connection.createStatement()) {

            statement.execute(sql);
            LOG.debug("query : {}", sql);

        } catch (SQLException e) {
            LOG.error(SCHEMA_SQL_EXECUTE_EXCEPTION_MESSAGE, e);
            throw new ExecuteSqlException(SCHEMA_SQL_EXECUTE_EXCEPTION_MESSAGE, e);
        }
    }

    private static String readSchemaSqlFileByPath(String path) {
        try {
            final URL url = DatabasePopulatorUtils.class.getClassLoader().getResource(path);
            final File file = new File(Objects.requireNonNull(url).getFile());
            return Files.readString(file.toPath());
        } catch (IOException e) {
            LOG.error(SCHEMA_SQL_FILE_READ_EXCEPTION_MESSAGE, e);
            throw new FileReadException(SCHEMA_SQL_FILE_READ_EXCEPTION_MESSAGE, e);
        }
    }
}
