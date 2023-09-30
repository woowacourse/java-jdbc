package org.springframework.jdbc.core;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.jdbc.core.SingleResult.from;

class SingleResultTest {

    @Test
    void getSingleResult() {
        // given
        final String expected = "결과";
        final List<String> results = List.of(expected);

        // when
        final Optional<String> actual = from(results);

        // then
        assertThat(actual).isNotEmpty();
        assertThat(actual.get()).isEqualTo(expected);
    }

    @Test
    void getSingleResult_MultipleResult_ExceptionThrown() {
        // given
        final List<String> results = List.of("결과1", "결과2");

        // when, then
        assertThatThrownBy(() -> from(results))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getSingleResult_NoResults_Empty() {
        // given
        final List<String> results = Collections.emptyList();

        // when
        final Optional<String> actual = from(results);

        // then
        assertThat(actual).isEmpty();
    }
}