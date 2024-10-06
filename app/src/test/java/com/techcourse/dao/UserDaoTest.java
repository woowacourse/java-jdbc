package com.techcourse.dao;

import static com.techcourse.fixture.UserFixture.DORA;
import static com.techcourse.fixture.UserFixture.GUGU;
import static org.assertj.core.api.Assertions.assertThat;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        userDao = new UserDao(DataSourceConfig.getInstance());
    }

    @Nested
    class FindAll {
        @Test
        void findAllWithOneUser() {
            // given
            userDao.insert(GUGU.user());

            // when
            final List<User> users = userDao.findAll();

            // then
            assertThat(users).isNotEmpty();
        }

        @Test
        void findAllWithTwoUser() {
            // given
            userDao.insert(GUGU.user());
            userDao.insert(DORA.user());

            // when
            final List<User> users = userDao.findAll();

            // then
            assertThat(users.size()).isEqualTo(2);
        }

        @Test
        void findAllWithZeroUser() {
            // when
            final List<User> users = userDao.findAll();

            // then
            assertThat(users).isEmpty();
        }
    }

    @Nested
    class FindById {
        @Test
        void findById() {
            // given
            userDao.insert(GUGU.user());

            // when
            final User user = userDao.findById(1L);

            // then
            assertThat(user.getAccount()).isEqualTo(GUGU.account());
        }
    }

    @Nested
    class FindByAccount {
        @Test
        void findByAccount() {
            // given
            userDao.insert(GUGU.user());

            // when
            final User user = userDao.findByAccount(GUGU.account());

            // then
            assertThat(user.getAccount()).isEqualTo(GUGU.account());
        }
    }

    @Nested
    class Insert {
        @Test
        void insert() {
            // when
            userDao.insert(DORA.user());

            // then
            final User actual = userDao.findById(1L);
            assertThat(actual.getAccount()).isEqualTo(DORA.account());
        }
    }

    @Nested
    class Update {
        @Test
        void update() {
            // given
            userDao.insert(GUGU.user());
            final User user = userDao.findById(1L);
            final String newPassword = "password99";
            user.changePassword(newPassword);

            // when
            userDao.update(user);

            // then
            final User actual = userDao.findById(1L);
            assertThat(actual.getPassword()).isEqualTo(newPassword);
        }
    }
}
