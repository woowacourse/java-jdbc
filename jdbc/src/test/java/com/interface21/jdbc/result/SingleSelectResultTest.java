package com.interface21.jdbc.result;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SingleSelectResultTest {
    @Test
    @DisplayName("대문자로 변환한 칼럼에 맞는 값을 가져온다.")
    void get_column_with_upper_column_name() {
        final SingleSelectResult result = new SingleSelectResult(
                Map.of("ID", 1)
        );

        final int id = result.getColumnValue("id");
        assertThat(id).isEqualTo(1);
    }

    @Test
    @DisplayName("칼럼에 맞는 값이 없으면 예외를 발생한다.")
    void throw_exception_when_not_exist_column() {
        final SingleSelectResult result = new SingleSelectResult(
                Map.of("id", 1)
        );

        assertThatThrownBy(() -> result.getColumnValue("notExist"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("칼럼과 다른 타입으로 값을 받으면 예외를 발생한다.")
    void throw_exception_when_not_equal_type() {
        final SingleSelectResult result = new SingleSelectResult(
                Map.of("id", 1)
        );

        assertThatThrownBy(() -> {
            final String wrongType = result.getColumnValue("id");
        }).isInstanceOf(IllegalArgumentException.class);
    }


}
