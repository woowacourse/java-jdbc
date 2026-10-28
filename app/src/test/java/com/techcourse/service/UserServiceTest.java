package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class UserServiceTest {

    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        dataSource = DataSourceConfig.getInstance();
        jdbcTemplate = new JdbcTemplate(dataSource);
        userDao = new UserDao(jdbcTemplate);

        DatabasePopulatorUtils.execute(dataSource);
        truncateTables();
        userDao.insert(new User("gugu", "password", "hkkang@woowahan.com"));
    }

    // 인메모리 DB가 테스트 간에 공유되므로, 매 테스트마다 행을 비우고 id를 1부터 다시 시작시킨다.
    private void truncateTables() {
        try (final var connection = dataSource.getConnection();
             final var statement = connection.createStatement()) {
            statement.execute("truncate table users restart identity");
            statement.execute("truncate table user_history restart identity");
        } catch (SQLException e) {
            throw new IllegalStateException("테이블 초기화에 실패했습니다.", e);
        }
    }

    private long countHistories(final long userId) {
        return jdbcTemplate.queryForObject(
                "select count(*) from user_history where user_id = ?",
                resultSet -> resultSet.getLong(1),
                userId
        );
    }

    @Test
    @DisplayName("비밀번호를 변경하면 바뀐 비밀번호로 조회되고, 변경 이력도 함께 저장된다")
    void testChangePassword() {
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final UserService userService = new TxUserService(new AppUserService(userDao, userHistoryDao), dataSource);

        final var newPassword = "qqqqq";
        final var createBy = "gugu";
        userService.changePassword(1L, newPassword, createBy);

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
        assertThat(countHistories(1L)).isEqualTo(1L);
    }

    @Test
    @DisplayName("이력 저장이 실패하면 DataAccessException을 던지고, 비밀번호 변경도 롤백된다")
    void testTransactionRollback() {
        // 트랜잭션 롤백 테스트를 위해 mock으로 교체
        final var userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        final UserService userService = new TxUserService(new AppUserService(userDao, userHistoryDao), dataSource);

        final var newPassword = "newPassword";
        final var createBy = "gugu";
        // 트랜잭션이 정상 동작하는지 확인하기 위해 의도적으로 MockUserHistoryDao에서 예외를 발생시킨다.
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, newPassword, createBy));

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isNotEqualTo(newPassword);
    }

    // TxUserService 없이 쓰면 쿼리마다 따로 autocommit된다.
    // 트랜잭션 처리가 AppUserService 안에 섞여 있지 않은지도 함께 확인한다.
    @Test
    @DisplayName("트랜잭션 없이 AppUserService만 쓰면, 이력 저장이 실패해도 앞서 실행한 비밀번호 변경은 그대로 남는다")
    void appUserServiceAloneDoesNotRollBack() {
        final UserService appUserService = new AppUserService(userDao, new MockUserHistoryDao(jdbcTemplate));

        assertThrows(DataAccessException.class,
                () -> appUserService.changePassword(1L, "newPassword", "gugu"));

        assertThat(appUserService.findById(1L).getPassword()).isEqualTo("newPassword");
    }

    // TransactionSynchronizationManager는 DataSource 인스턴스를 키로 쓴다.
    // 그래서 서비스와 DAO가 같은 DataSource(transactionDataSource)를 써야 DAO가 서비스의 트랜잭션 Connection을 찾는다.
    // 검증용 조회는 실제 DataSource를 쓰는 userDao로 해서, 트랜잭션 밖에서 본 커밋된 값을 읽는다.
    @Test
    @DisplayName("이력 INSERT가 DB 제약 조건 위반으로 실패하면 커밋하지 않고 직접 롤백해서, 비밀번호와 이력이 변경 전 그대로 남는다")
    void rollsBackPasswordChangeWhenHistoryInsertFailsInDatabase() throws SQLException {
        final var originalPassword = userDao.findById(1L).getPassword();

        try (final Connection connection = spy(dataSource.getConnection())) {
            final DataSource transactionDataSource = mock(DataSource.class);
            given(transactionDataSource.getConnection()).willReturn(connection);
            final var transactionJdbcTemplate = new JdbcTemplate(transactionDataSource);
            final UserService userService = new TxUserService(
                    new AppUserService(new UserDao(transactionJdbcTemplate), new UserHistoryDao(transactionJdbcTemplate)),
                    transactionDataSource
            );

            // created_by의 NOT NULL 제약조건을 위반해 실제 이력 INSERT에서 SQLException이 발생한다.
            assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", null))
                    .isInstanceOf(DataAccessException.class)
                    .hasRootCauseInstanceOf(SQLIntegrityConstraintViolationException.class);

            assertThat(userDao.findById(1L).getPassword()).isEqualTo(originalPassword);
            assertThat(countHistories(1L)).isZero();

            // H2의 close 시 자동 롤백만으로 통과하지 않도록 서비스의 명시적인 롤백도 확인한다.
            final var inOrder = inOrder(connection);
            inOrder.verify(connection).rollback();
            inOrder.verify(connection).close();
            verify(connection, never()).commit();
        }
    }

    @Test
    @DisplayName("비밀번호 UPDATE가 DB 제약 조건 위반으로 실패하면 이력은 저장하지 않고, 커밋 없이 직접 롤백해서 비밀번호가 그대로 남는다")
    void rollsBackWhenUserUpdateFailsInDatabase() throws SQLException {
        final var originalPassword = userDao.findById(1L).getPassword();
        final var userHistoryDao = mock(UserHistoryDao.class);

        try (final Connection connection = spy(dataSource.getConnection())) {
            final DataSource transactionDataSource = mock(DataSource.class);
            given(transactionDataSource.getConnection()).willReturn(connection);
            final var transactionJdbcTemplate = new JdbcTemplate(transactionDataSource);
            final UserService userService = new TxUserService(
                    new AppUserService(new UserDao(transactionJdbcTemplate), userHistoryDao),
                    transactionDataSource
            );

            // password의 NOT NULL 제약조건을 위반해 실제 UserDao의 UPDATE에서 예외가 발생한다.
            assertThatThrownBy(() -> userService.changePassword(1L, null, "gugu"))
                    .isInstanceOf(DataAccessException.class)
                    .hasRootCauseInstanceOf(SQLIntegrityConstraintViolationException.class);

            assertThat(userDao.findById(1L).getPassword()).isEqualTo(originalPassword);
            verifyNoInteractions(userHistoryDao);

            final var inOrder = inOrder(connection);
            inOrder.verify(connection).rollback();
            inOrder.verify(connection).close();
            verify(connection, never()).commit();
        }
    }
}
