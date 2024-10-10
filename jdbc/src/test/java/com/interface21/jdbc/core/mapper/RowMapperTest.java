package com.interface21.jdbc.core.mapper;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.sql.ResultSet;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.interface21.jdbc.core.mock.FakeResultSet;
import com.interface21.jdbc.core.mock.User;

class RowMapperTest {

    @DisplayName("ResultSet을 입력하면 쿼리 조회 결과를 파싱해서 객체로 변환해 반환한다.")
    @Test
    void mapping() {
        // Given
        final ResultSet resultSet = new FakeResultSet(List.of(List.of(
                1L, "kelly", "kellyPw1234!", "kelly@email.com"
        )));
        final RowMapper<User> rowMapper = new RowMapper<>(User.class);

        // When
        final User user = rowMapper.mapping(resultSet);

        // Then
        assertSoftly(softly -> {
            softly.assertThat(user.getId()).isEqualTo(1L);
            softly.assertThat(user.getAccount()).isEqualTo("kelly");
            softly.assertThat(user.getPassword()).isEqualTo("kellyPw1234!");
            softly.assertThat(user.getEmail()).isEqualTo("kelly@email.com");
        });
    }
}
