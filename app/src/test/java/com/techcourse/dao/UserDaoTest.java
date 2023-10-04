package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

class UserDaoTest {

    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
    private UserDao userDao;
    private DataSource dataSource;

    @BeforeEach
    void setup() throws SQLException {
        dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(dataSource);

        jdbcTemplate.update(dataSource.getConnection(), "TRUNCATE TABLE users RESTART IDENTITY");
        userDao = new UserDao(DataSourceConfig.getInstance());

        User user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(dataSource.getConnection(), user);
    }

    @Test
    void findAll() {
        List<User> users = userDao.findAll();

        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() throws SQLException {
        userDao.insert(dataSource.getConnection(), new User("gugu", "password", "hkkang@woowahan.com"));
        User user = userDao.findById(1L);

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        String account = "gugu";
        User user = userDao.findByAccount(account);

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void insert() throws SQLException {
        String account = "insert-gugu";
        User user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(dataSource.getConnection(), user);

        User actual = userDao.findById(2L);

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() throws SQLException {
        String newPassword = "password99";
        User user = userDao.findById(1L);
        user.changePassword(newPassword);

        userDao.update(dataSource.getConnection(), user);

        User actual = userDao.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    @DisplayName("단일 조회시, 결과가 2개 이상이면 예외가 발생한다.")
    void findByAccount_FailByMultipleResults() throws SQLException {
        //given
        User findUser = userDao.findByAccount("gugu");
        assertThat(findUser).isNotNull();

        User duplicateUser = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(dataSource.getConnection(), duplicateUser);

        //when then
        assertThatThrownBy(() -> userDao.findByAccount("gugu"))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("Incorrect Result Size ! Result  must be one");
    }

    @Test
    @DisplayName("단일 조회시, 결과가 없을 경우 예외가 발생한다.")
    void findById_FailByNotExistingResult() {
        //given
        List<User> users = userDao.findAll();

        assertThat(users).extractingResultOf("getId")
                .isNotEmpty()
                .doesNotContain(99L);

        //when then
        assertThatThrownBy(() -> userDao.findById(99L))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("Incorrect Result Size ! Result is null");
    }

}
