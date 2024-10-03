package com.techcourse;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;

public abstract class DatabaseTest {

    @BeforeEach
    void deleteData() throws SQLException {
        DataSource dataSourceInstance = DataSourceConfig.getInstance();
        Connection connection = dataSourceInstance.getConnection();
        connection.createStatement().execute("DROP TABLE IF EXISTS users");
        DatabasePopulatorUtils.execute(dataSourceInstance);
    }
}
