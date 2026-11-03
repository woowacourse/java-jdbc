package com.techcourse.dao;

import com.techcourse.support.TestDataSource;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserDaoTest {

    private UserDao userDao;
    private javax.sql.DataSource dataSource;

    @BeforeEach
    void setup() {
        dataSource = TestDataSource.create();
        DatabasePopulatorUtils.execute(dataSource);

        userDao = new UserDao(dataSource);
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
        final var user = userDao.findById(1L);

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        final var account = "gugu";
        final var user = userDao.findByAccount(account);

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void insert() {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        final var actual = userDao.findById(2L);

        assertThat(actual.getAccount()).isEqualTo(account);
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
    void missingUser() {
        assertThat(userDao.findById(-1L)).isNull();
        assertThat(userDao.findByAccount("missing")).isNull();
    }

    @Test
    void quotedAccountIsBoundAsData() {
        final var account = "o'reilly' OR '1'='1";
        userDao.insert(new User(account, "password", "test@example.com"));
        assertThat(userDao.findByAccount(account).getAccount()).isEqualTo(account);
        assertThat(userDao.findAll()).hasSize(2);
    }

    @Test
    void templateConstructor() {
        final var dao = new UserDao(new com.interface21.jdbc.core.JdbcTemplate(dataSource));
        assertThat(dao.findByAccount("gugu").getEmail()).isEqualTo("hkkang@woowahan.com");
    }
    @Test
    void duplicateAccountDoesNotChangeExistingUser() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                userDao.insert(new User("gugu", "other-password", "other@example.com")))
                .isInstanceOf(com.interface21.dao.DuplicateKeyException.class);
        assertThat(userDao.findAll()).hasSize(1);
        assertThat(userDao.findByAccount("gugu").checkPassword("password")).isTrue();
    }

    @Test
    void concurrentRegistrationsOnlyInsertOneUser() throws Exception {
        final var ready = new java.util.concurrent.CountDownLatch(2);
        final var start = new java.util.concurrent.CountDownLatch(1);
        try (var executor = java.util.concurrent.Executors.newFixedThreadPool(2)) {
            java.util.concurrent.Callable<Boolean> register = () -> {
                ready.countDown();
                if (!start.await(5, java.util.concurrent.TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Timed out waiting for start");
                }
                try {
                    userDao.insert(new User("concurrent", "password", "test@example.com"));
                    return true;
                } catch (com.interface21.dao.DuplicateKeyException e) {
                    return false;
                }
            };
            final var first = executor.submit(register);
            final var second = executor.submit(register);
            assertThat(ready.await(5, java.util.concurrent.TimeUnit.SECONDS)).isTrue();
            start.countDown();
            assertThat(java.util.List.of(first.get(5, java.util.concurrent.TimeUnit.SECONDS),
                    second.get(5, java.util.concurrent.TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(true, false);
        }
        assertThat(userDao.findAll()).hasSize(2);
        assertThat(userDao.findByAccount("concurrent").checkPassword("password")).isTrue();
    }
}
