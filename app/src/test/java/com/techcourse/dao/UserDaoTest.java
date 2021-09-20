package com.techcourse.dao;

import java.util.List;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import nextstep.jdbc.IncorrectResultSizeDataAccessException;
import nextstep.jdbc.JdbcTemplate;
import org.assertj.core.api.ThrowableAssert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserDaoTest {

    private static final JdbcTemplate JDBC_TEMPLATE = new JdbcTemplate(DataSourceConfig.getInstance());

    private UserDao userDao;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(JDBC_TEMPLATE);
        userDao = new UserDao(JDBC_TEMPLATE);

        final User user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @AfterEach
    void tearDown() {
        JDBC_TEMPLATE.update("DROP TABLE users");
    }

    @Test
    @DisplayName("저장된 유저 전체 검색 테스트")
    void findAllTest() {

        // when
        final List<User> users = userDao.findAll();

        // then
        assertThat(users).isNotEmpty();
    }

    @Test
    @DisplayName("ID에 해당하는 유저 검색 테스트")
    void findByIdTest() {

        // given
        final long id = 1L;

        // when
        final User user = userDao.findById(id);

        // then
        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    @DisplayName("주어진 ID와 일치하는 유저가 존재하지 않을 경우 예외 발생 테스트")
    void findByIdFailIfUserNotExistTest() {

        // given
        final long id = 2L;

        // when
        ThrowableAssert.ThrowingCallable callable = () -> userDao.findById(id);

        // then
        assertThatThrownBy(callable).isExactlyInstanceOf(IncorrectResultSizeDataAccessException.class)
                                    .hasMessage("파라미터에 해당하는 엔티티를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("account에 해당하는 유저 검색 테스트")
    void findByAccountTest() {

        // given
        final String account = "gugu";

        // when
        final User user = userDao.findByAccount(account);

        // then
        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    @DisplayName("주어진 account와 일치하는 유저가 존재하지 않을 경우 예외 발생 테스트")
    void findByAccountFailIfUserNotExistTest() {

        // given
        final String account = "seed";

        // when
        ThrowableAssert.ThrowingCallable callable = () -> userDao.findByAccount(account);

        // then
        assertThatThrownBy(callable).isExactlyInstanceOf(IncorrectResultSizeDataAccessException.class)
                                    .hasMessage("파라미터에 해당하는 엔티티를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("유저 저장 테스트")
    void insertTest() {

        // given
        final String account = "insert-gugu";
        final User user = new User(account, "password", "hkkang@woowahan.com");

        // when
        userDao.insert(user);

        // then
        final User actual = userDao.findById(2L);
        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void updateTest() {

        // given
        final String newPassword = "password99";
        final User user = userDao.findById(1L);
        user.changePassword(newPassword);

        // when
        userDao.update(user);

        // then
        final User actual = userDao.findById(1L);
        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
