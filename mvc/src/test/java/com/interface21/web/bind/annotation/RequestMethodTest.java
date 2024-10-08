package com.interface21.web.bind.annotation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RequestMethodTest {

    @DisplayName("메소드 이름으로 찾을 수 있다.")
    @Test
    void from() {
        RequestMethod method = RequestMethod.from("GET");

        assertThat(method).isEqualTo(RequestMethod.GET);
    }

    @DisplayName("존재하지 않는 메소드 이름은 찾을 수 없다.")
    @Test
    void incorrectName() {
        assertThatThrownBy(() -> RequestMethod.from("GETS"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 메소드 입니다.");
    }
}
