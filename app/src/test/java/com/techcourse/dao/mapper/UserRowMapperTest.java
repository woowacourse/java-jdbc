package com.techcourse.dao.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.techcourse.domain.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserRowMapperTest {

    private static final UserRowMapper userRowMapper = new UserRowMapper();

    @DisplayName("ResultSet을 받아 User 객체를 매핑한다.")
    @Test
    void mapRow() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("account")).thenReturn("kaki");
        when(resultSet.getString("password")).thenReturn("1234");
        when(resultSet.getString("email")).thenReturn("test@example.com");

        User user = userRowMapper.mapRow(resultSet);

        assertAll(
                () -> assertThat(user).isNotNull(),
                () -> assertThat(user.getId()).isEqualTo(1L),
                () -> assertThat(user.getAccount()).isEqualTo("kaki"),
                () -> assertThat(user.getPassword()).isEqualTo("1234"),
                () -> assertThat(user.getEmail()).isEqualTo("test@example.com")
        );
    }
}
