package com.techcourse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcOperations;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.transaction.JdbcTransactionManager;
import com.interface21.transaction.TransactionManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserServiceTest {

    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;
    private TransactionManager transactionManager;
    private UserService service;

    @BeforeEach
    void setUp() {
        jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        userDao = new UserDao(jdbcTemplate);
        transactionManager = new JdbcTransactionManager(DataSourceConfig.getInstance());
        service = new UserService(userDao, new UserHistoryDao(jdbcTemplate), transactionManager);

        jdbcTemplate.update("drop table if exists user_history");
        jdbcTemplate.update("drop table if exists users");
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        userDao.insert(new User("gugu", "password", "hkkang@woowahan.com"));
    }

    @Test
    void 비밀번호를_변경하면_새_비밀번호와_변경_이력이_저장된다() {
        // when
        service.changePassword(1L, "changed", "admin");

        // then
        User saved = userDao.findById(1L).orElseThrow();
        assertThat(saved.getPassword()).isEqualTo("changed");
        assertThat(readHistories(1L)).containsExactly(new HistoryRow("changed", "admin"));
    }

    @Test
    void 이력_저장에_실패하면_비밀번호_변경도_취소된다() {
        // when & then
        assertThatThrownBy(() -> service.changePassword(1L, "changed", null))
            .isInstanceOf(DataAccessException.class)
            .hasCauseInstanceOf(SQLException.class);

        User saved = userDao.findById(1L).orElseThrow();
        assertThat(saved.getPassword()).isEqualTo("password");
        assertThat(readHistories(1L)).isEmpty();
    }

    @Test
    void 이력을_저장한_뒤_예외가_발생하면_비밀번호와_이력을_모두_취소한다() {
        // given
        UserService service = new UserService(
            userDao,
            new MockUserHistoryDao(jdbcTemplate),
            transactionManager
        );

        // when & then
        assertThatThrownBy(() -> service.changePassword(1L, "changed", "admin"))
            .isInstanceOf(DataAccessException.class);

        User saved = userDao.findById(1L).orElseThrow();
        assertThat(saved.getPassword()).isEqualTo("password");
        assertThat(readHistories(1L)).isEmpty();
    }

    @Test
    void 존재하지_않는_사용자를_조회하면_값이_없음을_반환한다() {
        assertThat(service.findById(999L)).isEmpty();
    }

    @Test
    void 존재하지_않는_사용자의_비밀번호를_변경하면_예외를_던진다() {
        // when & then
        assertThatThrownBy(() -> service.changePassword(999L, "changed", "admin"))
            .isInstanceOf(UserNotFoundException.class);
        assertThat(readHistories(999L)).isEmpty();
    }

    @Test
    void 조회한_사용자가_수정되지_않으면_예외를_던지고_저장하지_않는다() {
        // given
        UserDao userDao = mock(UserDao.class);
        UserHistoryDao userHistoryDao = mock(UserHistoryDao.class);
        UserService service = new UserService(userDao, userHistoryDao, transactionManager);
        User user = new User(1L, "gugu", "password", "gugu@example.com");
        given(userDao.findById(1L)).willReturn(Optional.of(user));
        given(userDao.update(any(JdbcOperations.class), same(user))).willReturn(0);

        // when & then
        assertThatThrownBy(() -> service.changePassword(1L, "changed", "admin"))
            .isInstanceOf(UserNotFoundException.class);
        then(userDao).should().update(any(JdbcOperations.class), same(user));
        then(userHistoryDao).should(never()).log(any(JdbcOperations.class), any(UserHistory.class));
        assertThat(readHistories(1L)).isEmpty();
    }

    private List<HistoryRow> readHistories(final long userId) {
        return jdbcTemplate.query(
            "select password, created_by from user_history where user_id = ? order by id",
            row -> new HistoryRow(row.getString("password"), row.getString("created_by")),
            userId
        );
    }

    private record HistoryRow(String password, String createdBy) {
    }
}
