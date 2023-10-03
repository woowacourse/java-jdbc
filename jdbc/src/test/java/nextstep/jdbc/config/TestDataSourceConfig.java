package nextstep.jdbc.config;

import org.h2.jdbcx.JdbcDataSource;
import org.reflections.Reflections;

import javax.print.DocFlavor;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

public class TestDataSourceConfig {

    private static javax.sql.DataSource INSTANCE;

    public static javax.sql.DataSource getInstance() {
        if (Objects.isNull(INSTANCE)) {
            INSTANCE = createJdbcDataSource();
            executeSqls();
        }
        return INSTANCE;
    }

    private static JdbcDataSource createJdbcDataSource() {
        final var jdbcDataSource = new JdbcDataSource();
        jdbcDataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;");
        jdbcDataSource.setUser("");
        jdbcDataSource.setPassword("");
        return jdbcDataSource;
    }

    private static void executeSqls() {
        try (Connection conn = INSTANCE.getConnection();
             Statement statement = conn.createStatement()
        ) {
            String sql = readSqlFile();
            String[] statements = sql.split(";");
            for (String sqlStatement : statements) {
                statement.execute(sqlStatement);
            }
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String readSqlFile() throws IOException {
        URL resource = TestDataSourceConfig.class.getClassLoader().getResource("schema.sql");
        String sqlFile = resource.getFile();
        Path path = Paths.get(sqlFile);
        return new String(Files.readAllBytes(path));
    }

    private TestDataSourceConfig() {}
}
