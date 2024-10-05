package com.techcourse.dao.rowmapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.techcourse.domain.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserRowMapperTest {

    private UserRowMapper userRowMapper;

    @BeforeEach
    void setUp() {
        this.userRowMapper = new UserRowMapper();
    }

    @DisplayName("resultSet의 값이 없다면 null을 반환한다")
    @Test
    void returnNull_When_ResultSetHasNotNext() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);

        when(resultSet.next()).thenReturn(false);

        assertThat(userRowMapper.mapRow(resultSet)).isNull();
    }

    @DisplayName("resultSet을 바탕으로 유저를 매핑한다")
    @Test
    void mapUserRow() throws SQLException {
        User user = new User(1L, "testAccount", "testPwd", "testEmail");
        ResultSet resultSet = mock(ResultSet.class);

        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong(anyInt())).thenReturn(user.getId());
        when(resultSet.getString(anyInt())).thenReturn(user.getAccount(), user.getPassword(), user.getEmail());

        User mappedUser = userRowMapper.mapRow(resultSet);

        assertThat(mappedUser)
                .usingRecursiveComparison()
                .isEqualTo(user);
    }

    @DisplayName("sqlException 발생시 런타임 에러로 전환된다.")
    @Test
    void throwRuntimeException_When_ThrowSqlException() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenThrow(SQLException.class);

        assertThatThrownBy(()-> userRowMapper.mapRow(resultSet))
                .isInstanceOf(RuntimeException.class);
    }
}
