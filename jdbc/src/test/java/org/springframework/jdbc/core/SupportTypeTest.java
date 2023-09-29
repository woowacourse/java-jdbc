package org.springframework.jdbc.core;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class SupportTypeTest {

    @Test
    void findType_target_boolean() {
        //given
        boolean target = true;

        //when
        final SupportType type = SupportType.findType(target);

        //then
        Assertions.assertThat(type).isEqualTo(SupportType.BOOLEAN);
    }

    @Test
    void findType_target_byte() {
        //given
        byte target = 1;

        //when
        final SupportType type = SupportType.findType(target);

        //then
        Assertions.assertThat(type).isEqualTo(SupportType.BYTE);
    }

    @Test
    void findType_target_short() {
        //given
        short target = 1;

        //when
        final SupportType type = SupportType.findType(target);

        //then
        Assertions.assertThat(type).isEqualTo(SupportType.SHORT);
    }

    @Test
    void findType_target_integer() {
        //given
        int target = 1;

        //when
        final SupportType type = SupportType.findType(target);

        //then
        Assertions.assertThat(type).isEqualTo(SupportType.INTEGER);
    }

    @Test
    void findType_target_long() {
        //given
        long target = 1L;

        //when
        final SupportType type = SupportType.findType(target);

        //then
        Assertions.assertThat(type).isEqualTo(SupportType.LONG);
    }

    @Test
    void findType_target_float() {
        //given
        float target = 1.0f;

        //when
        final SupportType type = SupportType.findType(target);

        //then
        Assertions.assertThat(type).isEqualTo(SupportType.FLOAT);
    }

    @Test
    void findType_target_double() {
        //given
        double target = 1.0;

        //when
        final SupportType type = SupportType.findType(target);

        //then
        Assertions.assertThat(type).isEqualTo(SupportType.DOUBLE);
    }

    @Test
    void findType_target_string() {
        //given
        String target = "hello";

        //when
        final SupportType type = SupportType.findType(target);

        //then
        Assertions.assertThat(type).isEqualTo(SupportType.STRING);
    }

    @Test
    void findType_target_date() {
        //given
        Date target = new Date();

        //when
        final SupportType type = SupportType.findType(target);

        //then
        Assertions.assertThat(type).isEqualTo(SupportType.DATE);
    }

    @Test
    void findType_target_time() {
        //given
        Time target = new Time(1000);

        //when
        final SupportType type = SupportType.findType(target);

        //then
        Assertions.assertThat(type).isEqualTo(SupportType.TIME);
    }

    @Test
    void findType_target_timestamp() {
        //given
        Timestamp target = Timestamp.valueOf(LocalDateTime.now());

        //when
        final SupportType type = SupportType.findType(target);

        //then
        Assertions.assertThat(type).isEqualTo(SupportType.TIMESTAMP);
    }
}
