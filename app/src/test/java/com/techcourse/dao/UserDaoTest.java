package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
    @DisplayName("저장된 회원 목록을 돌려준다")
    void findAll() {
        final var users = userDao.findAll();

        assertThat(users).anySatisfy(user -> {
            assertThat(user.getAccount()).isEqualTo("gugu");
            assertThat(user.getPassword()).isEqualTo("password");
            assertThat(user.getEmail()).isEqualTo("hkkang@woowahan.com");
        });
    }

    @Test
    void findById() {
        final var user = userDao.findById(1L);

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    @DisplayName("해당 ID의 회원이 없으면 null을 돌려준다")
    void findByIdReturnsNullWhenUserDoesNotExist() {
        assertThat(userDao.findById(999L)).isNull();
    }

    @Test
    @DisplayName("계정으로 회원을 찾는다")
    void findByAccount() {
        final var account = "gugu";
        final var user = userDao.findByAccount(account);

        assertThat(user.getId()).isPositive();
        assertThat(user.getAccount()).isEqualTo(account);
        assertThat(user.getPassword()).isEqualTo("password");
        assertThat(user.getEmail()).isEqualTo("hkkang@woowahan.com");
    }

    @Test
    @DisplayName("일치하는 계정이 없으면 null을 돌려준다")
    void findByAccountReturnsNullWhenUserDoesNotExist() {
        assertThat(userDao.findByAccount("missing-account")).isNull();
    }

    @Test
    void insert() {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        final var actual = userDao.findById(2L);

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    @DisplayName("변경된 회원 정보를 DB에 저장한다")
    void update() {
        final var newPassword = "password99";
        final var user = userDao.findById(1L);
        user.changePassword(newPassword);

        userDao.update(user);

        final var actual = userDao.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
