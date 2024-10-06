package com.techcourse.dao.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.techcourse.domain.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserRowMapperTest {

    private ResultSet resultSet;
    private UserRowMapper userRowMapper;

    @BeforeEach
    void setUp() {
        resultSet = mock(ResultSet.class);
        userRowMapper = new UserRowMapper();
    }

    @DisplayName("UserRowMapper가 ResultSet으로부터 User 객체를 정상적으로 매핑한다.")
    @Test
    void mapRow() throws SQLException {
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("account")).thenReturn("account");
        when(resultSet.getString("password")).thenReturn("password");
        when(resultSet.getString("email")).thenReturn("email");

        User user = userRowMapper.mapRow(resultSet);

        assertAll(
                () -> assertThat(user.getId()).isEqualTo(1L),
                () -> assertThat(user.getAccount()).isEqualTo("account"),
                () -> assertThat(user.getPassword()).isEqualTo("password"),
                () -> assertThat(user.getEmail()).isEqualTo("email")
        );

        verify(resultSet, times(1)).getLong("id");
        verify(resultSet, times(1)).getString("account");
        verify(resultSet, times(1)).getString("password");
        verify(resultSet, times(1)).getString("email");
    }
}
