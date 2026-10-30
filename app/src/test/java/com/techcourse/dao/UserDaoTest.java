package com.techcourse.dao;

import com.interface21.dao.IncorrectResultSizeDataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserDaoTest {

    private UserDao userDao;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        final var dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(dataSource);
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update("truncate table user_history restart identity");
        jdbcTemplate.update("truncate table users restart identity");

        userDao = new UserDao(dataSource);
        userDao.insert(new User("gugu", "password", "hkkang@woowahan.com"));
    }

    @Test
    @DisplayName("모든 사용자를 조회한다")
    void find_all() {
        userDao.insert(new User("brown", "brown-password", "brown@woowahan.com"));

        final var users = userDao.findAll();

        assertThat(users)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(
                        new User(1L, "gugu", "password", "hkkang@woowahan.com"),
                        new User(2L, "brown", "brown-password", "brown@woowahan.com"));
    }

    @Test
    @DisplayName("사용자가 없으면 빈 목록을 반환한다")
    void find_all_with_no_users() {
        jdbcTemplate.update("delete from users");

        assertThat(userDao.findAll()).isEmpty();
    }

    @Test
    @DisplayName("아이디로 사용자를 조회한다")
    void find_by_id() {
        assertThat(userDao.findById(1L))
                .usingRecursiveComparison()
                .isEqualTo(new User(1L, "gugu", "password", "hkkang@woowahan.com"));
    }

    @Test
    @DisplayName("계정으로 사용자를 조회한다")
    void find_by_account() {
        assertThat(userDao.findByAccount("gugu"))
                .usingRecursiveComparison()
                .isEqualTo(new User(1L, "gugu", "password", "hkkang@woowahan.com"));
    }

    @Test
    @DisplayName("사용자를 찾지 못하면 null을 반환한다")
    void find_missing_user() {
        assertThat(userDao.findById(999L)).isNull();
        assertThat(userDao.findByAccount("missing")).isNull();
    }

    @Test
    @DisplayName("사용자를 저장한다")
    void insert() {
        userDao.insert(new User("insert-gugu", "new-password", "insert@woowahan.com"));

        assertThat(userDao.findById(2L))
                .usingRecursiveComparison()
                .isEqualTo(new User(2L, "insert-gugu", "new-password", "insert@woowahan.com"));
    }

    @Test
    @DisplayName("변경한 비밀번호를 저장한다")
    void update() {
        final var user = userDao.findById(1L);
        user.changePassword("password99");

        userDao.update(user);

        assertThat(userDao.findById(1L))
                .usingRecursiveComparison()
                .isEqualTo(new User(1L, "gugu", "password99", "hkkang@woowahan.com"));
    }

    @Test
    @DisplayName("아이디가 일치하는 사용자의 정보만 수정한다")
    void update_only_matching_user() {
        userDao.insert(new User("brown", "brown-password", "brown@woowahan.com"));

        userDao.update(new User(1L, "renamed-gugu", "changed-password", "changed@woowahan.com"));

        assertThat(userDao.findById(1L))
                .usingRecursiveComparison()
                .isEqualTo(new User(1L, "renamed-gugu", "changed-password", "changed@woowahan.com"));
        assertThat(userDao.findById(2L))
                .usingRecursiveComparison()
                .isEqualTo(new User(2L, "brown", "brown-password", "brown@woowahan.com"));
    }

    @Test
    @DisplayName("수정할 사용자가 없으면 예외를 던지고 기존 사용자 정보를 유지한다")
    void update_missing_user() {
        final var user = new User(999L, "missing", "changed-password", "changed@woowahan.com");

        assertThatThrownBy(() -> userDao.update(user))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("999");
        assertThat(userDao.findAll())
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(new User(1L, "gugu", "password", "hkkang@woowahan.com"));
    }

    @Test
    @DisplayName("주입받은 JdbcTemplate으로 저장과 조회를 수행한다")
    void use_injected_jdbc_template() {
        final var dao = new UserDao(jdbcTemplate);
        dao.insert(new User("template", "before", "template@woowahan.com"));
        final var user = dao.findByAccount("template");
        user.changePassword("after");

        dao.update(user);

        assertThat(dao.findById(user.getId()))
                .usingRecursiveComparison()
                .isEqualTo(new User(2L, "template", "after", "template@woowahan.com"));
        assertThat(dao.findAll()).hasSize(2);
    }

    @Test
    @DisplayName("계정에 SQL 구문이 포함되어도 문자열 값으로 처리한다")
    void bind_account_parameter() {
        final var account = "gugu' OR '1' = '1";
        assertThat(userDao.findByAccount(account)).isNull();

        userDao.insert(new User(account, "quoted-password", "quoted@woowahan.com"));

        assertThat(userDao.findByAccount(account))
                .usingRecursiveComparison()
                .isEqualTo(new User(2L, account, "quoted-password", "quoted@woowahan.com"));
        assertThat(userDao.findAll()).hasSize(2);
    }

    @Test
    @DisplayName("같은 계정으로 두 명 이상 조회되면 예외를 던진다")
    void find_by_duplicate_account() {
        userDao.insert(new User("gugu", "another-password", "another@woowahan.com"));

        assertThatThrownBy(() -> userDao.findByAccount("gugu"))
                .isInstanceOf(IncorrectResultSizeDataAccessException.class);
    }
}
