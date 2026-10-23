package com.techcourse.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.techcourse.domain.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import org.h2.jdbc.JdbcResultSet;
import org.junit.jupiter.api.Test;

class UserRowMapperTest {

    @Test
    void resultSet_을_단일_User_로_매핑한다() {
        // given
        UserRowMapper rowMapper = new UserRowMapper();
        try (ResultSet resultSet = mock(ResultSet.class)) {
            when(resultSet.next()).thenReturn(true, false);
            when(resultSet.getLong("id")).thenReturn(1L);
            when(resultSet.getString("account")).thenReturn("user1");
            when(resultSet.getString("password")).thenReturn("password");
            when(resultSet.getString("email")).thenReturn("user1@test.com");

            User expected = new User(1L, "user1", "password", "user1@test.com");

            // when
            User actual = rowMapper.mapRow(resultSet);

            // then
            assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}