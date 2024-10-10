package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.interface21.dao.DuplicateKeyException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
        final var user = userDao.findById(1L).orElseThrow();

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        final var account = "gugu";
        final var user = userDao.findByAccount(account).orElseThrow();

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void insert() {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        final var actual = userDao.findById(2L).orElseThrow();

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() {
        final var newPassword = "password99";
        final var user = userDao.findById(1L).orElseThrow();
        user.changePassword(newPassword);

        userDao.update(user);

        final var actual = userDao.findById(1L).orElseThrow();

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    @DisplayName("동일한 account 가 2개 존재하면 조회할 때 예외가 발생한다.")
    void findByAccountDuplicated() {
        // given
        final var inputUser = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(inputUser);
        final var account = "gugu";

        // when & then
        assertThatCode(() -> userDao.findByAccount(account))
                .isInstanceOf(IncorrectResultSizeDataAccessException.class);
    }

    @Test
    @DisplayName("이미 존재하는 기본키를 가진 유저 저장 시 예외가 발생한다.")
    void insertWithExistPrimaryKey() {
        // given
        UserDao customUserDao = new UserDao(DataSourceConfig.getInstance()) {
            @Override
            public void insert(User user) {
                String sql = "INSERT INTO users (id, account, password, email) VALUES (?, ?, ?, ?)";
                jdbcTemplate.update(sql, user.getId(), user.getAccount(), user.getPassword(), user.getEmail());
            }
        };
        final var user = new User(1, "gugu", "password", "hkkang@woowahan.com");

        // when & then
        assertThatCode(() -> customUserDao.insert(user))
                .isInstanceOf(DuplicateKeyException.class);
    }
}
