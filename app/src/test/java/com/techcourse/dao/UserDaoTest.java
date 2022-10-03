package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.JdbcTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setup() throws IOException {
        DataSource dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(dataSource);

        userDao = new UserDao(dataSource);
        cleanUp(dataSource);
    }

    private static void cleanUp(final DataSource dataSource) throws IOException {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        URL url = Thread.currentThread().getContextClassLoader().getResource("cleanup.sql");
        File file = new File(url.getFile());
        Files.readAllLines(file.toPath())
                .forEach(jdbcTemplate::update);
    }

    @Test
    void findAll() {
        User user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);

        List<User> users = userDao.findAll();

        assertThat(users).usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                .containsOnly(user);
    }

    @Test
    void findById() {
        User user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);

        User actual = userDao.findById(1L);

        assertThat(actual).usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(user);
    }

    @Test
    void findByAccount() {
        User user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);

        User actual = userDao.findByAccount("gugu");

        assertThat(actual).usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(user);
    }

    @Test
    void insert() {
        String account = "insert-gugu";
        User user = new User(account, "password", "hkkang@woowahan.com");

        assertDoesNotThrow(() -> userDao.insert(user));
    }

    @Test
    void update() {
        User user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);

        String newPassword = "password99";
        User updateUser = userDao.findById(1L);
        updateUser.changePassword(newPassword);

        userDao.update(updateUser);

        User actual = userDao.findById(1L);

        assertThat(actual).usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(updateUser);
    }
}
