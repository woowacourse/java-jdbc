package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setUp() {
        JdbcTemplate template = new JdbcTemplate(DataSourceConfig.getInstance());
        template.update("drop table if exists user_history");
        template.update("drop table if exists users");
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        userDao = new UserDao(template);
        userDao.insert(new User("gugu", "password", "gugu@example.com"));
    }

    @Test
    void 사용자_목록을_조회하면_저장한_사용자가_모두_포함된다() {
        userDao.insert(new User("other", "secret", "other@example.com"));
        assertThat(userDao.findAll()).extracting(User::getAccount).containsExactlyInAnyOrder("gugu", "other");
    }

    @Test
    void ID로_조회하면_그_ID를_가진_사용자를_반환한다() {
        assertThat(userDao.findById(1L)).hasValueSatisfying(user -> {
            assertThat(user.getId()).isEqualTo(1L);
            assertThat(user.getAccount()).isEqualTo("gugu");
        });
    }

    @Test
    void 존재하지_않는_ID로_조회하면_값이_없음을_반환한다() {
        assertThat(userDao.findById(999L)).isEmpty();
    }

    @Test
    void 계정으로_조회하면_그_계정을_가진_사용자를_반환한다() {
        assertThat(userDao.findByAccount("gugu")).hasValueSatisfying(user ->
            assertThat(user.getAccount()).isEqualTo("gugu"));
    }

    @Test
    void 존재하지_않는_계정으로_조회하면_값이_없음을_반환한다() {
        assertThat(userDao.findByAccount("missing")).isEmpty();
    }

    @Test
    void 사용자를_저장하면_계정과_비밀번호와_이메일을_다시_조회할_수_있다() {
        userDao.insert(new User("other", "secret", "other@example.com"));
        assertThat(userDao.findByAccount("other")).hasValueSatisfying(user -> {
            assertThat(user.getAccount()).isEqualTo("other");
            assertThat(user.getPassword()).isEqualTo("secret");
            assertThat(user.getEmail()).isEqualTo("other@example.com");
        });
    }

    @Test
    void 비밀번호를_수정한_뒤_다시_조회하면_새_비밀번호가_저장돼_있다() {
        User user = userDao.findById(1L).orElseThrow();
        user.changePassword("changed");
        final int updatedCount = userDao.update(user);

        assertThat(updatedCount).isEqualTo(1);
        assertThat(userDao.findById(1L)).hasValueSatisfying(saved -> {
            assertThat(saved.getPassword()).isEqualTo("changed");
            assertThat(saved.getAccount()).isEqualTo("gugu");
            assertThat(saved.getEmail()).isEqualTo("gugu@example.com");
        });
    }

    @Test
    void 존재하지_않는_사용자를_수정하면_0을_반환한다() {
        User user = new User(999L, "missing", "secret", "missing@example.com");

        final int updatedCount = userDao.update(user);

        assertThat(updatedCount).isZero();
    }

    @Test
    void 이미_저장된_계정으로_사용자를_저장하면_예외를_던진다() {
        assertThatThrownBy(() -> userDao.insert(new User("gugu", "other", "other@example.com")))
            .isInstanceOf(DataAccessException.class).hasCauseInstanceOf(SQLException.class);
    }
}
