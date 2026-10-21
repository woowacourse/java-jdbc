package com.techcourse.dao;

import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserDaoTest {

    private UserDao userDao;
    private Connection keepAliveConnection;

    @BeforeEach
    void setUp() throws SQLException {
        final var dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:user-dao-test-" + UUID.randomUUID());

        // DAO가 커넥션을 닫아도 테스트가 끝날 때까지 메모리 DB를 유지한다.
        keepAliveConnection = dataSource.getConnection();
        DatabasePopulatorUtils.execute(dataSource);
        userDao = new UserDao(dataSource);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (keepAliveConnection != null) {
            keepAliveConnection.close();
        }
    }

    @Test
    void findAll_MultipleUsers_ReturnsAllUsers() {
        userDao.insert(new User("gugu", "password", "gugu@example.com"));
        userDao.insert(new User("ethan", "another-password", "ethan@example.com"));

        final var users = userDao.findAll();

        assertThat(users)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(
                        new User(1L, "gugu", "password", "gugu@example.com"),
                        new User(2L, "ethan", "another-password", "ethan@example.com"));
    }

    @Test
    void findAll_NoUsers_ReturnsEmptyList() {
        assertThat(userDao.findAll()).isEmpty();
    }

    @Test
    void findById_ExistingId_ReturnsMatchingUser() {
        userDao.insert(new User("gugu", "password", "gugu@example.com"));
        userDao.insert(new User("ethan", "another-password", "ethan@example.com"));

        final var user = userDao.findById(2L);

        assertThat(user).usingRecursiveComparison()
                .isEqualTo(new User(2L, "ethan", "another-password", "ethan@example.com"));
    }

    @Test
    void findById_NonexistentId_ReturnsNull() {
        userDao.insert(new User("gugu", "password", "gugu@example.com"));

        assertThat(userDao.findById(999L)).isNull();
    }

    @Test
    void findByAccount_ExistingAccount_ReturnsMatchingUser() {
        userDao.insert(new User("gugu", "password", "gugu@example.com"));
        userDao.insert(new User("ethan", "another-password", "ethan@example.com"));

        final var user = userDao.findByAccount("ethan");

        assertThat(user).usingRecursiveComparison()
                .isEqualTo(new User(2L, "ethan", "another-password", "ethan@example.com"));
    }

    @Test
    void findByAccount_NonexistentAccount_ReturnsNull() {
        userDao.insert(new User("gugu", "password", "gugu@example.com"));

        assertThat(userDao.findByAccount("unknown")).isNull();
    }

    @Test
    void insert_NewUser_PersistsAllFields() {
        userDao.insert(new User("gugu", "password", "gugu@example.com"));

        final var actual = userDao.findById(1L);

        assertThat(actual).usingRecursiveComparison()
                .isEqualTo(new User(1L, "gugu", "password", "gugu@example.com"));
    }

    @Test
    void update_ExistingUser_ChangesPassword() {
        userDao.insert(new User("gugu", "password", "gugu@example.com"));
        final var user = userDao.findById(1L);
        user.changePassword("password99");

        userDao.update(user);

        final var actual = userDao.findById(1L);

        assertThat(actual).usingRecursiveComparison()
                .isEqualTo(new User(1L, "gugu", "password99", "gugu@example.com"));
    }

    @Test
    void update_MultipleUsers_ChangesOnlyTargetUser() {
        userDao.insert(new User("gugu", "password", "gugu@example.com"));
        userDao.insert(new User("ethan", "another-password", "ethan@example.com"));
        final var target = userDao.findById(2L);
        target.changePassword("new-password");

        userDao.update(target);

        assertThat(userDao.findById(1L)).usingRecursiveComparison()
                .isEqualTo(new User(1L, "gugu", "password", "gugu@example.com"));
        assertThat(userDao.findById(2L)).usingRecursiveComparison()
                .isEqualTo(new User(2L, "ethan", "new-password", "ethan@example.com"));
    }
}
