package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.exception.IncorrectResultSizeDataAccessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class UserDaoTest {

    private static final Logger LOG = LoggerFactory.getLogger(UserDaoTest.class);
    private static final DataSource DATA_SOURCE = DataSourceConfig.getInstance();
    private static final JdbcTemplate JDBC_TEMPLATE = new JdbcTemplate(DATA_SOURCE);

    private UserDao userDao;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DATA_SOURCE);
        userDao = new UserDao(JDBC_TEMPLATE);
    }

    @AfterEach
    void tearDown() throws SQLException {
        String sql = "drop table if exists users";
        try (final Connection connection = DATA_SOURCE.getConnection();
            final PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.execute();
            LOG.debug("query : {}", sql);
        }
    }

    @DisplayName("findAll() 테스트 - 성공 - 조회 결과가 0개일 때")
    @Test
    void findAll_When_ResultSizeIsEmpty() {
        // given
        // when
        final List<User> users = userDao.findAll();

        // then
        assertThat(users).isEmpty();
    }

    @DisplayName("findAll() 테스트 - 성공 - 조회 결과가 3개일 때")
    @Test
    void findAll_When_ResultSizeIsNotEmpty() {
        // given
        final User user1 = new User("gugu1", "password", "hkkang@woowahan.com");
        final User user2 = new User("gugu2", "password", "hkkang@woowahan.com");
        final User user3 = new User("gugu3", "password", "hkkang@woowahan.com");
        userDao.insert(user1);
        userDao.insert(user2);
        userDao.insert(user3);

        // when
        final List<User> users = userDao.findAll();

        // then
        assertThat(users).extracting("account")
            .containsExactlyInAnyOrder(user1.getAccount(), user2.getAccount(), user3.getAccount());
    }

    @DisplayName("findByAccount() 테스트 - 성공 - 조회 결과가 1개일 때")
    @Test
    void findByAccount_When_ResultSizeIsOne() {
        // given
        final User user1 = new User("gugu1", "password", "hkkang@woowahan.com");
        userDao.insert(user1);

        // when
        final User foundUser = userDao.findByAccount(user1.getAccount());

        // then
        assertThat(foundUser.getAccount()).isEqualTo(user1.getAccount());
    }

    @DisplayName("findByAccount() 테스트 - 예외 발생 - 조회 결과가 0개일 때")
    @Test
    void findByAccount_Exception_When_ResultIsEmpty() {
        // given
        // when
        final User foundUser = userDao.findByAccount("test-account");

        // then
        assertThat(foundUser).isNull();
    }

    @DisplayName("findByAccount() 테스트 - 예외 발생 - 조회 결과가 1개보다 많을 때")
    @Test
    void findByAccount_Exception_When_ResultSizeIsOverOne() {
        // given
        final String account = "gugu";
        final User user1 = new User(account, "password", "hkkang@woowahan.com");
        final User user2 = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user1);
        userDao.insert(user2);

        // when
        // then
        assertThatThrownBy(() -> userDao.findByAccount(account))
            .isInstanceOf(IncorrectResultSizeDataAccessException.class);
    }

    @DisplayName("findById() 테스트 - 성공 - 조회 결과가 1개일 때")
    @Test
    void findById() {
        // given
        final User user1 = new User("gugu1", "password", "hkkang@woowahan.com");
        userDao.insert(user1);

        // when
        final User foundUser = userDao.findById(1L);

        // then
        assertThat(foundUser.getAccount()).isEqualTo(user1.getAccount());
    }

    @DisplayName("findById() 테스트 - 예외 발생 - 조회 결과가 0개일 때")
    @Test
    void findById_Exception_When_ResultIsEmpty() {
        // given
        // when
        final User foundUser = userDao.findById(1L);

        // then
        assertThat(foundUser).isNull();
    }

    @DisplayName("insert() 테스트 - 성공")
    @Test
    void insert() {
        // given
        final User user = new User("insert-gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);

        // when
        final User foundUser = userDao.findById(1L);

        // then
        assertThat(foundUser.getAccount()).isEqualTo(user.getAccount());
    }

    @DisplayName("update() 테스트 - 성공")
    @Test
    void update() {
        // given
        final User user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);

        // when
        final User foundUser = userDao.findById(1L);
        final String newPassword = "password99";
        foundUser.changePassword(newPassword);
        userDao.update(foundUser);

        final User actual = userDao.findById(1L);

        // then
        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
