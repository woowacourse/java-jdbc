package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.DataAccessException;
import nextstep.jdbc.JdbcTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        DataSource dataSource = DataSourceConfig.getInstance();
        userDao = new UserDao(new JdbcTemplate(dataSource));
        userDao.deleteAll();
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @DisplayName("모든 유저를 조회한다.")
    @Test
    void findAll() {
        List<User> users = userDao.findAll();
        assertThat(users).map(User::getAccount)
                .containsExactly("gugu");
    }

    @DisplayName("id로 유저를 조회한다.")
    @Test
    void findById() {
        User user = userDao.findById(1L);

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @DisplayName("id로 유저를 찾지 못하면 예외를 던진다.")
    @Test
    void findById_exception() {
        assertThatThrownBy(() -> userDao.findById(2L))
                .isInstanceOf(DataAccessException.class);
    }

    @DisplayName("account로 유저를 조회한다.")
    @Test
    void findByAccount() {
        final var account = "gugu";
        final var user = userDao.findByAccount(account);

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @DisplayName("account로 유저를 찾지 못하면 예외를 던진다.")
    @Test
    void findByAccount_exception() {
        assertThatThrownBy(() -> userDao.findByAccount("invalidAccount"))
                .isInstanceOf(DataAccessException.class);
    }

    @DisplayName("유저를 생성한다.")
    @Test
    void insert() {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        final var actual = userDao.findById(2L);

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @DisplayName("유저의 정보를 수정한다.")
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
