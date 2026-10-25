package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class UserServiceTest {

    private static final String ORIGINAL_PASSWORD = "password";

    private DataSource dataSource;
    private Connection databaseAnchor;
    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = spy(DataSourceConfig.createDataSource("jdbc:h2:mem:user-service-" + UUID.randomUUID()));
        databaseAnchor = dataSource.getConnection();
        jdbcTemplate = new JdbcTemplate(dataSource);
        userDao = spy(new UserDao(jdbcTemplate));
        DatabasePopulatorUtils.execute(dataSource);
        userDao.insert(new User("gugu", ORIGINAL_PASSWORD, "hkkang@woowahan.com"));
    }

    @AfterEach
    void closeDatabase() throws SQLException {
        databaseAnchor.close();
    }

    @Test
    @DisplayName("조회·수정·이력에 같은 연결 하나를 전달하고 변경자와 시각까지 커밋한다")
    void testChangePassword() throws SQLException {
        final var historyDao = spy(new UserHistoryDao(jdbcTemplate));
        final var userService = service(historyDao);
        final var connectionCaptor = ArgumentCaptor.forClass(Connection.class);
        final var historyCaptor = ArgumentCaptor.forClass(UserHistory.class);
        clearInvocations(dataSource, userDao, historyDao);

        userService.changePassword(1L, "newPassword", "operator");

        verify(dataSource, times(1)).getConnection();
        verify(userDao).findById(connectionCaptor.capture(), eq(1L));
        final var sharedConnection = connectionCaptor.getValue();
        verify(userDao).update(same(sharedConnection), any(User.class));
        verify(historyDao).log(same(sharedConnection), historyCaptor.capture());
        assertThat(sharedConnection.isClosed()).isTrue();
        assertThat(userDao.findById(1L).getPassword()).isEqualTo("newPassword");
        final var saved = jdbcTemplate.queryForObject("select * from user_history where user_id = ?", row ->
                new SavedHistory(row.getLong("user_id"), row.getString("account"), row.getString("password"),
                        row.getString("email"), row.getString("created_by"),
                        row.getTimestamp("created_at").toLocalDateTime()), 1L);
        assertThat(saved.userId()).isEqualTo(1L);
        assertThat(saved.account()).isEqualTo("gugu");
        assertThat(saved.password()).isEqualTo("newPassword");
        assertThat(saved.email()).isEqualTo("hkkang@woowahan.com");
        assertThat(saved.createdBy()).isEqualTo("operator");
        assertThat(saved.createdAt()).isCloseTo(historyCaptor.getValue().getCreatedAt(), within(1, ChronoUnit.MICROS));
        assertThat(historyCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("이력 저장이 실패하면 원래 비밀번호를 정확히 복원하고 이력을 남기지 않는다")
    void testTransactionRollback() {
        assertThrows(DataAccessException.class,
                () -> service(new MockUserHistoryDao(jdbcTemplate)).changePassword(1L, "newPassword", "operator"));

        assertOriginalState();
    }

    @Test
    @DisplayName("실제 이력 INSERT 뒤 실패해도 두 쓰기를 롤백하고 다음 요청은 정상 실행한다")
    void failureAfterHistoryInsertRollsBackBothWritesAndAllowsTheNextRequest() {
        final var failure = new DataAccessException("이력 INSERT 뒤 실패");
        final var historyDao = new UserHistoryDao(jdbcTemplate) {
            @Override
            public void log(final Connection connection, final UserHistory history) {
                super.log(connection, history);
                throw failure;
            }
        };

        assertThat(assertThrows(DataAccessException.class,
                () -> service(historyDao).changePassword(1L, "newPassword", "operator"))).isSameAs(failure);
        assertOriginalState();

        service(new UserHistoryDao(jdbcTemplate)).changePassword(1L, "nextPassword", "nextOperator");

        assertThat(userDao.findById(1L).getPassword()).isEqualTo("nextPassword");
        assertThat(historyCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("이력의 NOT NULL 제약 위반도 앞선 사용자 수정을 롤백한다")
    void historyConstraintFailureRollsBackThePassword() {
        final var failure = assertThrows(DataAccessException.class,
                () -> service(new UserHistoryDao(jdbcTemplate)).changePassword(1L, "newPassword", null));

        assertThat(failure.getCause()).isInstanceOf(SQLException.class);
        assertOriginalState();
    }

    @Test
    @DisplayName("사용자 수정 SQL의 제약 위반 시 기존 비밀번호를 유지하고 이력을 만들지 않는다")
    void updateConstraintFailureLeavesTheOriginalState() {
        final var failure = assertThrows(DataAccessException.class,
                () -> service(new UserHistoryDao(jdbcTemplate)).changePassword(1L, null, "operator"));

        assertThat(failure.getCause()).isInstanceOf(SQLException.class);
        assertOriginalState();
    }

    @Test
    @DisplayName("같은 연결에서 발생한 다건 조회 예외도 앞선 SQL을 롤백하고 연결을 닫는다")
    void multipleRowsDuringTheTransactionRollBackAnEarlierWrite() throws SQLException {
        userDao.insert(new User("other", "otherOriginal", "other@example.com"));
        final var usedConnection = new AtomicReference<Connection>();
        final var checkingDao = new UserDao(jdbcTemplate) {
            @Override
            public User findById(final Connection connection, final Long id) {
                usedConnection.set(connection);
                jdbcTemplate.update(connection, "update users set password = ? where id = ?", "intermediate", id);
                return jdbcTemplate.queryForObject(connection, "select * from users order by id", row ->
                        new User(row.getLong("id"), row.getString("account"), row.getString("password"),
                                row.getString("email")));
            }
        };
        final var userService = new UserService(dataSource, checkingDao, new UserHistoryDao(jdbcTemplate));

        final var failure = assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, "newPassword", "operator"));

        assertThat(failure).hasMessage("Expected at most one row, but found two or more").hasNoCause();
        assertThat(usedConnection.get().isClosed()).isTrue();
        assertOriginalState();
        assertThat(userDao.findById(2L).getPassword()).isEqualTo("otherOriginal");
    }

    @Test
    @DisplayName("연속 변경은 이전 이력을 덮어쓰지 않고 각 변경 내용을 보존한다")
    void successiveChangesPreserveEachHistorySnapshot() {
        final var userService = service(new UserHistoryDao(jdbcTemplate));

        userService.changePassword(1L, "firstPassword", "firstOperator");
        userService.changePassword(1L, "secondPassword", "secondOperator");

        assertThat(userDao.findById(1L).getPassword()).isEqualTo("secondPassword");
        final var changes = jdbcTemplate.query("select password, created_by from user_history order by id", row ->
                row.getString("password") + ":" + row.getString("created_by"));
        assertThat(changes).containsExactly("firstPassword:firstOperator", "secondPassword:secondOperator");
    }

    private UserService service(final UserHistoryDao historyDao) {
        return new UserService(dataSource, userDao, historyDao);
    }

    private void assertOriginalState() {
        assertThat(userDao.findById(1L).getPassword()).isEqualTo(ORIGINAL_PASSWORD);
        assertThat(historyCount()).isZero();
    }

    private long historyCount() {
        return jdbcTemplate.queryForObject("select count(*) from user_history", row -> row.getLong(1));
    }

    private record SavedHistory(long userId, String account, String password, String email,
                                String createdBy, LocalDateTime createdAt) {}
}
