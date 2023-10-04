package org.springframework.jdbc;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import org.h2.jdbcx.JdbcDataSource;

public class TestDataSourceConfig {

    private static final String SCHEMA_SQL = "schema.sql";
    private static final String DATA_SQL = "data.sql";
    private static final List<String> SQL_FILES = List.of(SCHEMA_SQL, DATA_SQL);

    private static javax.sql.DataSource INSTANCE;

    public static javax.sql.DataSource getInstance() {
        if (Objects.isNull(INSTANCE)) {
            INSTANCE = createJdbcDataSource();
            init();
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

    private static void init() {
        try (Connection conn = INSTANCE.getConnection()) {
            for (String sqlFile : SQL_FILES) {
                String sql = readSql(sqlFile);
                for (String query : sql.split(";")) {
                    PreparedStatement ps = conn.prepareStatement(query);
                    ps.execute();
                }

            }
        } catch (SQLException | IOException | URISyntaxException e) {
            throw new RuntimeException("테스트 db 환경 구축 중 문제가 발생했습니다.: " + e);
        }
    }

    private static String readSql(String sql) throws IOException, URISyntaxException {
        URL resource = TestDataSourceConfig.class.getClassLoader()
                .getResource(sql);
        Path path = Path.of(resource.toURI());
        return Files.readString(path);
    }
}
