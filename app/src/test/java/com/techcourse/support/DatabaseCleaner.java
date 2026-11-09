package com.techcourse.support;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;

public class DatabaseCleaner {

    private DatabaseCleaner() {
    }

    public static void clean(DataSource dataSource) {
        try (Connection conn = dataSource.getConnection();
             Statement statement = conn.createStatement()) {
            statement.execute("TRUNCATE TABLE user_history RESTART IDENTITY");
            statement.execute("TRUNCATE TABLE users RESTART IDENTITY");
        } catch (SQLException e) {
            throw new IllegalStateException("테스트 DB를 초기화할 수 없습니다.", e);
        }
    }
}
