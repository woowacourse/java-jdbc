package com.techcourse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("사용자 비밀번호 변경과 이력 저장")
class UserServiceTest {

    private JdbcDataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;
    private UserService userService;

    @BeforeEach
    void setUp() {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        DatabasePopulatorUtils.execute(dataSource);
        jdbcTemplate = new JdbcTemplate(dataSource);
        userDao = new UserDao(jdbcTemplate);
        userDao.insert(new User("gugu", "password", "hkkang@woowahan.com"));
        userService = new TxUserService(new AppUserService(userDao, new UserHistoryDao(jdbcTemplate)), dataSource);
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("SHUTDOWN");
    }

    @Test
    @DisplayName("비밀번호를 변경하면 변경 내용과 변경자 및 시각을 이력에 저장한다")
    void testChangePassword() {
        final var startedAt = LocalDateTime.now();

        userService.changePassword(1L, "new-password", "admin");

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        assertThat(userDao.findById(1L).getPassword()).isEqualTo("new-password");
        final var histories = histories();
        assertThat(histories).hasSize(1);
        final var history = histories.getFirst();
        assertThat(history.userId()).isEqualTo(1L);
        assertThat(history.account()).isEqualTo("gugu");
        assertThat(history.password()).isEqualTo("new-password");
        assertThat(history.email()).isEqualTo("hkkang@woowahan.com");
        assertThat(history.createdBy()).isEqualTo("admin");
        assertThat(history.createdAt()).isBetween(startedAt.minusSeconds(1), LocalDateTime.now().plusSeconds(1));
    }

    @Test
    @DisplayName("이력 저장 중 예외가 발생하면 비밀번호 변경을 롤백한다")
    void testTransactionRollback() {
        final var failingHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        final var service = new TxUserService(new AppUserService(userDao, failingHistoryDao), dataSource);

        assertThatThrownBy(() -> service.changePassword(1L, "new-password", "admin"))
                .isInstanceOf(DataAccessException.class);

        assertPasswordAndHistoryUnchanged();
    }

    @Test
    @DisplayName("이력 INSERT가 데이터베이스 제약 조건을 위반하면 비밀번호 변경을 롤백한다")
    void rollsBackPasswordWhenHistoryInsertFailsInDatabase() {
        assertThatThrownBy(() -> userService.changePassword(1L, "new-password", null))
                .isInstanceOf(DataAccessException.class)
                .hasCauseInstanceOf(SQLException.class);

        assertPasswordAndHistoryUnchanged();
    }

    @Test
    @DisplayName("이력 저장 직후 실패하면 비밀번호 변경과 이력 저장을 모두 롤백한다")
    void rollsBackBothWritesWhenFailureOccursAfterHistoryInsert() {
        final var failure = new IllegalStateException("이력 저장 후 실패");
        final var failingHistoryDao = new UserHistoryDao(jdbcTemplate) {
            @Override
            public void log(final UserHistory userHistory) {
                super.log(userHistory);
                throw failure;
            }
        };
        final var service = new TxUserService(new AppUserService(userDao, failingHistoryDao), dataSource);

        assertThatThrownBy(() -> service.changePassword(1L, "new-password", "admin"))
                .isSameAs(failure);

        assertPasswordAndHistoryUnchanged();
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 비밀번호를 변경하면 데이터와 이력이 바뀌지 않는다")
    void missingUserDoesNotWriteHistory() {
        assertThatThrownBy(() -> userService.changePassword(Long.MAX_VALUE, "new-password", "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");

        assertPasswordAndHistoryUnchanged();
    }

    @Test
    @DisplayName("성공과 실패를 연속 호출해도 이전 트랜잭션의 연결을 재사용하지 않는다")
    void startsFreshTransactionsAfterSuccessAndFailure() {
        userService.changePassword(1L, "first-password", "admin");
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();

        assertThatThrownBy(() -> userService.changePassword(1L, "failed-password", null))
                .isInstanceOf(DataAccessException.class);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        assertThat(userDao.findById(1L).getPassword()).isEqualTo("first-password");

        userService.changePassword(1L, "second-password", "admin");

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        assertThat(userDao.findById(1L).getPassword()).isEqualTo("second-password");
        assertThat(histories()).extracting(PasswordHistory::password)
                .containsExactlyInAnyOrder("first-password", "second-password");
    }

    private void assertPasswordAndHistoryUnchanged() {
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        assertThat(userDao.findById(1L).getPassword()).isEqualTo("password");
        assertThat(histories()).isEmpty();
    }

    private List<PasswordHistory> histories() {
        return jdbcTemplate.query("select * from user_history", row -> new PasswordHistory(
                row.getLong("user_id"), row.getString("account"), row.getString("password"),
                row.getString("email"), row.getTimestamp("created_at").toLocalDateTime(),
                row.getString("created_by")));
    }

    private record PasswordHistory(long userId, String account, String password, String email,
                                   LocalDateTime createdAt, String createdBy) {
    }
}
