package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.IncorrectRowSizeException;
import org.springframework.jdbc.RowNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        userDao = new UserDao(DataSourceConfig.getInstance());
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void findAll() {
        final var users = userDao.findAll();

        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        final var user = userDao.findById(1L);

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findById_not_match() {
        // when && then
        assertThatThrownBy(() -> userDao.findById(623623L))
            .isInstanceOf(RowNotFoundException.class);
    }

    @Test
    void findByAccount() {
        // given
        String account = "hyunseo";
        userDao.insert(new User(account, "hyunseo159", "hs@kakao.com"));

        // when
        User user = userDao.findByAccount(account);

        // then
        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void findByAcoount_not_match() {
        // when && then
        assertThatThrownBy(() -> userDao.findByAccount("notgugu"))
            .isInstanceOf(RowNotFoundException.class);
    }

    @Test
    void findByAcoount_throw_multiple_row() {
        // given
        String account = "duplicate";
        userDao.insert(new User(account, "hyunseo159", "hs@kakao.com"));
        userDao.insert(new User(account, "hyunseo159", "hs@kakao.com"));

        // when && then
        assertThatThrownBy(() -> userDao.findByAccount(account))
            .isInstanceOf(IncorrectRowSizeException.class);
    }

    @Test
    void insert() {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        final var actual = userDao.findById(2L);

        assertThat(actual.getAccount()).isEqualTo(account);
    }
}
