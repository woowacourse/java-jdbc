package com.techcourse.dao;

import static com.techcourse.fixture.UserFixture.DORA;
import static com.techcourse.fixture.UserFixture.GUGU;
import static org.assertj.core.api.Assertions.assertThat;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.rowmapper.UserRowMapper;
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
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        UserRowMapper userRowMapper = new UserRowMapper();
        this.userDao = new UserDao(jdbcTemplate, userRowMapper);
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
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
            final User user = userDao.findById(1L).get();

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
            final User user = userDao.findByAccount(GUGU.account()).get();

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
            final User actual = userDao.findById(1L).get();
            assertThat(actual.getAccount()).isEqualTo(DORA.account());
        }
    }

    @Nested
    class Update {
        @Test
        void update() {
            // given
            userDao.insert(GUGU.user());
            final User user = userDao.findById(1L).get();
            final String newPassword = "password99";
            user.changePassword(newPassword);

            // when
            userDao.update(user);

            // then
            final User actual = userDao.findById(1L).get();
            assertThat(actual.getPassword()).isEqualTo(newPassword);
        }
    }
}
