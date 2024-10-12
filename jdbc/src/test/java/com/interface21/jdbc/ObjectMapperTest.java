package com.interface21.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ObjectMapperTest {

    ResultSet resultSet = mock(ResultSet.class);

    @BeforeEach
    void setUp() throws SQLException {
        given(resultSet.getLong(1)).willReturn(1L);
        given(resultSet.getString(2)).willReturn("kirby");
        given(resultSet.getString(3)).willReturn("1234");
        given(resultSet.getString(4)).willReturn("kirby@naver.com");
    }

    @Test
    void mapToObject() throws SQLException {
       // given
        ObjectMapper<TestUser> objectMapper = (rs) -> new TestUser(
                rs.getLong(1),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4)
        );

        // when
        TestUser testUser = objectMapper.mapToObject(resultSet);

        // then
        assertAll(
                () -> assertThat(testUser.getId()).isEqualTo(1L),
                () -> assertThat(testUser.getAccount()).isEqualTo("kirby"),
                () -> assertThat(testUser.getPassword()).isEqualTo("1234"),
                () -> assertThat(testUser.getEmail()).isEqualTo("kirby@naver.com")
        );
    }
}
