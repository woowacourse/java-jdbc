package com.techcourse.dao;

import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import static org.assertj.core.api.Assertions.assertThat;

class UserPersistenceTest {
    @TempDir
    Path directory;

    @Test
    void usersSurviveDatabaseCloseAndReinitialization() {
        final var first = dataSource();
        DatabasePopulatorUtils.execute(first);
        new UserDao(first).insert(new User("persistent", "password", "test@example.com"));

        // Without DB_CLOSE_DELAY, closing the last connection closes this file database.
        final var reopened = dataSource();
        DatabasePopulatorUtils.execute(reopened);
        assertThat(new UserDao(reopened).findByAccount("persistent").getEmail())
                .isEqualTo("test@example.com");
    }

    private JdbcDataSource dataSource() {
        final var source = new JdbcDataSource();
        source.setURL("jdbc:h2:file:" + directory.resolve("users"));
        source.setUser("");
        source.setPassword("");
        return source;
    }
}
