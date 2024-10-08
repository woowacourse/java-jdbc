package com.techcourse.support.jdbc.init;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;

public class TestDatabasePopulatorUtils {

    public static void execute(final DataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            String sql = """
                    create table if not exists users (
                        id bigint auto_increment,
                        account varchar(100) not null,
                        password varchar(100) not null,
                        email varchar(100) not null,
                        primary key(id)
                    );
                    """;
            statement.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void cleanUp(final DataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            String sql = """
                    SET REFERENTIAL_INTEGRITY FALSE;
                    TRUNCATE TABLE users;
                    ALTER TABLE users ALTER COLUMN id RESTART WITH 1;
                    SET REFERENTIAL_INTEGRITY TRUE;
                    """;
            statement.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
