package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.util.UUID;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setup() {
        final var dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:user-dao-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1;");
        DatabasePopulatorUtils.execute(dataSource);

        final var jdbcTemplate = new JdbcTemplate(dataSource);
        userDao = new UserDao(jdbcTemplate);
        jdbcTemplate.update(
                "insert into users (account, password, email) values (?, ?, ?)",
                "gugu", "password", "hkkang@woowahan.com"
        );
    }

    @Test
    void findById() {
        final var user = userDao.findById(1L);

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void update() {
        final var newPassword = "password99";
        final var user = userDao.findById(1L);
        user.changePassword(newPassword);

        userDao.update(user);

        final var actual = userDao.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
