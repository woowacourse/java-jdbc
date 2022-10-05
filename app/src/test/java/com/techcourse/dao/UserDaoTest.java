package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;

import nextstep.jdbc.JdbcTemplate;

class UserDaoTest {

    private UserDao userDao;
    private DataSource dataSource;
    private long userId;

    @BeforeEach
    void setUp() {
        dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.init(dataSource);

        userDao = new UserDao(new JdbcTemplate(dataSource));
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
        userId = userDao.findByAccount("gugu").getId();
    }

    @AfterEach
    void tearDown() {
        DatabasePopulatorUtils.clear(dataSource);
    }

    @DisplayName("모든 사용자 목록 조회")
    @Test
    void findAll() {
        final var users = userDao.findAll();

        assertThat(users).isNotEmpty();
    }

    @DisplayName("id 를 통해 사용자 정보 조회")
    @Test
    void findById() {
        final var user = userDao.findById(userId);

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @DisplayName("account 를 통해 사용자 정보 조회")
    @Test
    void findByAccount() {
        final var account = "gugu";
        final var user = userDao.findByAccount(account);

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @DisplayName("사용자 정보 저장")
    @Test
    void insert() {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        final var actual = userDao.findByAccount(account);

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @DisplayName("사용자 비밀번호 수정")
    @Test
    void update() {
        final var newPassword = "password99";
        final var user = userDao.findById(userId);
        user.changePassword(newPassword);

        userDao.updatePassword(user);

        final var actual = userDao.findById(userId);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
