package org.springframework.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@DisplayNameGeneration(ReplaceUnderscores.class)
@SuppressWarnings("NonAsciiCharacters")
class ObjectConverterTest {

    @Mock
    ResultSet resultSet;

    @BeforeEach
    void setting() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void ResultSet_을_원하는_타입의_객체로_변환한다() throws SQLException {
        // given
        given(resultSet.getObject(any(), any(Class.class)))
            .willReturn("pooh", 100);

        // when
        Pooh result = ObjectConverter.convert(resultSet, Pooh.class);

        // then
        assertAll(
            () -> assertThat(result.age).isEqualTo(100),
            () -> assertThat(result.name).isEqualTo("pooh")
        );
    }

    public static class Pooh {

        private String name;
        private Integer age;

        public Pooh() {
        }
    }
}
