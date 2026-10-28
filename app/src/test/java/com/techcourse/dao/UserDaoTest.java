package com.techcourse.dao;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setUp() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        new JdbcTemplate(DataSourceConfig.getInstance()).update("truncate table users restart identity");

        userDao = new UserDao(DataSourceConfig.getInstance());
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void findAll() {
        userDao.insert(new User("pobi", "password99", "pobi@woowahan.com"));

        final var users = userDao.findAll();

        assertThat(users)
                .extracting(User::getId, User::getAccount, User::getPassword, User::getEmail)
                .containsExactlyInAnyOrder(
                        tuple(1L, "gugu", "password", "hkkang@woowahan.com"),
                        tuple(2L, "pobi", "password99", "pobi@woowahan.com")
                );
    }

    @Test
    void 사용자가_없으면_빈_목록을_반환한다() {
        new JdbcTemplate(DataSourceConfig.getInstance()).update("delete from users");

        final var users = userDao.findAll();

        assertThat(users).isEmpty();
    }

    @Test
    void findById() {
        final var user = userDao.findById(1L);

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getAccount()).isEqualTo("gugu");
        assertThat(user.getPassword()).isEqualTo("password");
        assertThat(user.getEmail()).isEqualTo("hkkang@woowahan.com");
    }

    @Test
    void 존재하지_않는_아이디로_조회하면_null을_반환한다() {
        final var user = userDao.findById(999L);

        assertThat(user).isNull();
    }

    @Test
    void JdbcTemplate으로_생성한_DAO도_아이디로_사용자를_조회한다() {
        final var jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        final var dao = new UserDao(jdbcTemplate);

        final var user = dao.findById(1L);

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        final var account = "pobi";
        userDao.insert(new User(account, "password99", "pobi@woowahan.com"));

        final var user = userDao.findByAccount(account);

        assertThat(user.getId()).isEqualTo(2L);
        assertThat(user.getAccount()).isEqualTo(account);
        assertThat(user.getPassword()).isEqualTo("password99");
        assertThat(user.getEmail()).isEqualTo("pobi@woowahan.com");
    }

    @Test
    void 존재하지_않는_계정으로_조회하면_null을_반환한다() {
        final var user = userDao.findByAccount("unknown");

        assertThat(user).isNull();
    }

    @Test
    void insert() {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        final var actual = userDao.findById(2L);

        assertThat(actual.getAccount()).isEqualTo(account);
        assertThat(actual.getPassword()).isEqualTo(user.getPassword());
        assertThat(actual.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void JdbcTemplate으로_생성한_DAO도_사용자를_저장한다() {
        final var jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        final var dao = new UserDao(jdbcTemplate);
        final var user = new User("template-gugu", "password99", "template@woowahan.com");

        dao.insert(user);

        final var actual = userDao.findById(2L);
        assertThat(actual.getAccount()).isEqualTo(user.getAccount());
        assertThat(actual.getPassword()).isEqualTo(user.getPassword());
        assertThat(actual.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void update() {
        final var newPassword = "password99";
        final var user = userDao.findById(1L);
        user.changePassword(newPassword);

        userDao.update(user);

        final var actual = userDao.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void 외부_연결로_사용자를_수정하고_커밋하면_변경이_저장된다() throws SQLException {
        final var user = userDao.findById(1L);
        final var newPassword = "password99";
        user.changePassword(newPassword);

        try (final var connection = DataSourceConfig.getInstance().getConnection()) {
            connection.setAutoCommit(false);

            userDao.update(connection, user);

            connection.commit();
        }

        final var actual = userDao.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void 외부_연결로_수정할_사용자가_없으면_예외가_발생한다() throws SQLException {
        final var user = userDao.findById(1L);
        new JdbcTemplate(DataSourceConfig.getInstance()).update("delete from users where id = ?", user.getId());

        try (final var connection = DataSourceConfig.getInstance().getConnection()) {
            assertThatThrownBy(() -> userDao.update(connection, user))
                    .isInstanceOf(DataAccessException.class)
                    .hasMessage("사용자 수정 결과는 1건이어야 합니다. 실제 수정 건수: 0");
        }
    }

    @Test
    void 외부_연결로_사용자를_수정하고_롤백하면_기존_비밀번호가_유지된다() throws SQLException {
        final var user = userDao.findById(1L);
        final var originalPassword = user.getPassword();
        user.changePassword("password99");

        try (final var connection = DataSourceConfig.getInstance().getConnection()) {
            connection.setAutoCommit(false);

            userDao.update(connection, user);

            connection.rollback();
        }

        final var actual = userDao.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(originalPassword);
    }
}
