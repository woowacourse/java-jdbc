package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        truncateUsers(DataSourceConfig.getInstance());

        userDao = new UserDao(new JdbcTemplate(DataSourceConfig.getInstance()));
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    @DisplayName("findAll은 저장된 사용자들을 목록으로 반환한다")
    void findAll() {
        final var users = userDao.findAll();

        assertThat(users).isNotEmpty();
    }

    @Test
    @DisplayName("findById는 id에 해당하는 사용자를 반환한다")
    void findById() {
        final var user = userDao.findById(1L);

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    @DisplayName("findByAccount는 account가 일치하는 사용자를 반환한다")
    void findByAccount() {
        final var account = "gugu";
        final var user = userDao.findByAccount(account);

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    @DisplayName("insert는 새 사용자를 저장해서, 다음 id로 조회할 수 있게 한다")
    void insert() {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        final var actual = userDao.findById(2L);

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    @DisplayName("update는 사용자 정보를 수정한다")
    void update() {
        final var newPassword = "password99";
        final var user = userDao.findById(1L);
        user.changePassword(newPassword);

        userDao.update(user);

        final var actual = userDao.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    // 서비스가 하듯 autocommit을 끈 Connection을 묶어두면, Connection을 넘기지 않아도 DAO가 그 트랜잭션에 참여한다.
    @Test
    @DisplayName("update는 묶여 있는 트랜잭션에 참여해서, 그 트랜잭션을 롤백하면 변경이 남지 않는다")
    void updateParticipatesInBoundTransaction() throws SQLException {
        final var originalPassword = userDao.findById(1L).getPassword();
        final var user = userDao.findById(1L);
        user.changePassword("password99");

        final DataSource dataSource = DataSourceConfig.getInstance();
        final Connection connection = dataSource.getConnection();
        connection.setAutoCommit(false);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        try {
            userDao.update(user);
        } finally {
            connection.rollback();
            TransactionSynchronizationManager.unbindResource(dataSource);
            connection.close();
        }

        assertThat(userDao.findById(1L).getPassword()).isEqualTo(originalPassword);
    }

    // 같은 Connection으로 읽어야 커밋 전인 자기 변경을 본다. 트랜잭션 밖의 다른 Connection으로 읽으면 변경 전 값이 보인다.
    @Test
    @DisplayName("findById는 묶여 있는 트랜잭션 안에서 조회하면 커밋 전 변경을 읽고, 트랜잭션 밖에서는 변경 전 값이 보인다")
    void findByIdSeesUncommittedChangeInBoundTransaction() throws SQLException {
        final var user = userDao.findById(1L);
        final var originalPassword = user.getPassword();
        user.changePassword("password99");

        final DataSource dataSource = DataSourceConfig.getInstance();
        final Connection connection = dataSource.getConnection();
        connection.setAutoCommit(false);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        try {
            userDao.update(user);

            assertThat(userDao.findById(1L).getPassword()).isEqualTo("password99");
            assertThat(readPasswordOutsideTransaction(1L)).isEqualTo(originalPassword);
        } finally {
            connection.rollback();
            TransactionSynchronizationManager.unbindResource(dataSource);
            connection.close();
        }

        assertThat(userDao.findById(1L).getPassword()).isEqualTo(originalPassword);
    }

    // TransactionSynchronizationManager를 거치지 않고 따로 Connection을 얻어, 트랜잭션 밖에서 본 커밋된 값을 읽는다.
    private String readPasswordOutsideTransaction(final long id) throws SQLException {
        try (final var connection = DataSourceConfig.getInstance().getConnection();
             final var statement = connection.prepareStatement("select password from users where id = ?")) {
            statement.setLong(1, id);
            try (final var resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getString("password");
            }
        }
    }

    // 인메모리 DB가 테스트 간에 공유되므로, 매 테스트마다 행을 비우고 id를 1부터 다시 시작시킨다.
    private void truncateUsers(final DataSource dataSource) {
        try (final var connection = dataSource.getConnection();
             final var statement = connection.createStatement()) {
            statement.execute("truncate table users restart identity");
        } catch (SQLException e) {
            throw new IllegalStateException("users 테이블 초기화에 실패했습니다.", e);
        }
    }
}
