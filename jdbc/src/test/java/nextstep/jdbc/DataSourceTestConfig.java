package nextstep.jdbc;

import org.h2.jdbcx.JdbcDataSource;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

public class DataSourceTestConfig {
    private static javax.sql.DataSource INSTANCE;

    public static javax.sql.DataSource getInstance() throws SQLException {
        if (Objects.isNull(INSTANCE)) {
            INSTANCE = createJdbcDataSource();
        }
        return INSTANCE;
    }

    private static JdbcDataSource createJdbcDataSource() throws SQLException {
        JdbcDataSource jdbcDataSource = new JdbcDataSource();
        jdbcDataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;");
        jdbcDataSource.setUser("");
        jdbcDataSource.setPassword("");
        Connection connection = jdbcDataSource.getConnection();
        connection.prepareStatement(readSqlFile("init.sql")).executeUpdate();
        connection.prepareStatement(readSqlFile("data.sql")).executeUpdate();
        return jdbcDataSource;
    }

    private static String readSqlFile(String fileName) {
        StringBuilder st = new StringBuilder();

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(getAbstractPath(fileName)));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                st.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return st.toString();
    }

    private static String getAbstractPath(final String fileName) {
        URL resource = DataSourceTestConfig.class.getClassLoader().getResource(fileName);
        return Objects.requireNonNull(resource).getFile();
    }
}
