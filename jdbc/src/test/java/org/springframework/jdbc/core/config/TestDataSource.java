package org.springframework.jdbc.core.config;

import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class TestDataSource {

    private static DataSource INSTANCE;

    public static DataSource getInstance() {
        if (INSTANCE == null) {
            INSTANCE = generateDataSource();
            executeSchemaSql();
        }
        return INSTANCE;
    }

    private static DataSource generateDataSource() {
        final JdbcDataSource jdbcDataSource = new JdbcDataSource();
        jdbcDataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;");
        jdbcDataSource.setUser("test");
        jdbcDataSource.setPassword("test");
        return jdbcDataSource;
    }

    private static void executeSchemaSql() {
        try (final Connection conn = INSTANCE.getConnection();
             final Statement statement = conn.createStatement()
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
        URL resource = TestDataSource.class.getClassLoader().getResource("schema.sql");
        String sqlFile = resource.getFile();
        Path path = Paths.get(sqlFile);
        return new String(Files.readAllBytes(path));
    }

    private TestDataSource() {
    }
}
