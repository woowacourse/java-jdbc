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

    @Test
    void existingTableReceivesUniqueConstraintWithoutLosingUsers() {
        final var source = dataSource();
        final var jdbc = new com.interface21.jdbc.core.JdbcTemplate(source);
        jdbc.update("create table users (id bigint auto_increment primary key, "
                + "account varchar(100) not null, password varchar(100) not null, email varchar(100) not null)");
        final var dao = new UserDao(source);
        dao.insert(new User("existing", "password", "test@example.com"));
        DatabasePopulatorUtils.execute(source);
        DatabasePopulatorUtils.execute(source);
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                dao.insert(new User("existing", "other", "other@example.com")))
                .isInstanceOf(com.interface21.dao.DuplicateKeyException.class);
        assertThat(dao.findAll()).hasSize(1);
        assertThat(dao.findByAccount("existing").checkPassword("password")).isTrue();
    }

    @Test
    void existingDuplicatesFailInitializationWithoutDeletingData() {
        final var source = dataSource();
        final var jdbc = new com.interface21.jdbc.core.JdbcTemplate(source);
        jdbc.update("create table users (id bigint auto_increment primary key, "
                + "account varchar(100) not null, password varchar(100) not null, email varchar(100) not null)");
        final var dao = new UserDao(source);
        dao.insert(new User("existing", "first", "first@example.com"));
        dao.insert(new User("existing", "second", "second@example.com"));
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> DatabasePopulatorUtils.execute(source))
                .isInstanceOf(com.interface21.dao.DataAccessException.class);
        assertThat(dao.findAll()).hasSize(2);
    }

    private JdbcDataSource dataSource() {
        final var source = new JdbcDataSource();
        source.setURL("jdbc:h2:file:" + directory.resolve("users"));
        source.setUser("");
        source.setPassword("");
        return source;
    }
}
