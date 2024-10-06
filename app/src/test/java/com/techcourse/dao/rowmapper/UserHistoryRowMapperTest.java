package com.techcourse.dao.rowmapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.techcourse.domain.UserHistory;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserHistoryRowMapperTest {

    private UserHistoryRowMapper userHistoryRowMapper;

    @BeforeEach
    void setUp() {
        this.userHistoryRowMapper = new UserHistoryRowMapper();
    }


    @DisplayName("resultSet을 바탕으로 유저 히스토리를 매핑한다")
    @Test
    void mapUserRow() throws SQLException {
        LocalDateTime now = LocalDateTime.now();
        UserHistory userHistory = new UserHistory(1L, 1L, "testAccount", "testPwd", "testEmail", now, "testCreateBy");
        ResultSet resultSet = mock(ResultSet.class);

        when(resultSet.getLong(anyString())).thenReturn(userHistory.getId(), userHistory.getUserId());
        when(resultSet.getObject(anyString(), eq(LocalDateTime.class))).thenReturn(now);
        when(resultSet.getString(anyString()))
                .thenReturn(userHistory.getAccount(), userHistory.getPassword(), userHistory.getEmail(), userHistory.getCreateBy());

        UserHistory mappedUserHistory = userHistoryRowMapper.mapRow(resultSet);

        assertThat(mappedUserHistory)
                .usingRecursiveComparison()
                .isEqualTo(userHistory);
    }

    @DisplayName("sqlException 발생시 런타임 에러로 전환된다.")
    @Test
    void throwRuntimeException_When_ThrowSqlException() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getLong(anyString())).thenThrow(SQLException.class);

        assertThatThrownBy(() -> userHistoryRowMapper.mapRow(resultSet))
                .isInstanceOf(RuntimeException.class);
    }
}
