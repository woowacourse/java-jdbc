package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import nextstep.exception.SqlUpdateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserDaoTest {

    private static final UserDao userDao;
    private static final AtomicLong userNameSalt = new AtomicLong(1L);

    private User insertUser;

    static {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        userDao = new UserDao();
    }

    @BeforeEach
    void setUp() {
        insertUser = getUser();
        userDao.insert(insertUser);
    }

    @Test
    void findAll() {
        final List<User> users = userDao.findAll();

        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        final User user = userDao.findById(insertUser.getId());

        assertThat(user.getAccount()).isEqualTo(insertUser.getAccount());
    }

    @Test
    void findByAccount() {
        final User user = userDao.findByAccount(insertUser.getAccount());

        assertThat(user.getAccount()).isEqualTo(insertUser.getAccount());
    }

    @Test
    void update() {
        final String newPassword = "password99";
        final User updateUser = new User(insertUser.getId(), insertUser.getAccount(), newPassword, insertUser.getEmail());
        userDao.update(updateUser);

        final User actual = userDao.findById(insertUser.getId());

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void duplicateUserAccountException() {
        assertThatThrownBy(() -> userDao.insert(insertUser))
            .isExactlyInstanceOf(SqlUpdateException.class);
    }

    private User getUser() {
        long salt = userNameSalt.incrementAndGet();
        return new User(salt,"gugu" + salt, "password", "hkkang@woowahan.com");
    }
}
