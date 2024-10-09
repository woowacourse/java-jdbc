package com.interface21.jdbc.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WrapperToPrimitiveConverterTest {

    @Test
    @DisplayName("Integer 을 int 으로 변환한다.")
    void Integer_convert_int() {
        final Integer integer = Integer.valueOf(128);
        final Class<?> clazz = WrapperToPrimitiveConverter.getPrimitiveClass(integer.getClass());
        assertThat(clazz).isEqualTo(int.class);
    }

    @Test
    @DisplayName("Long 을 long 으로 변환한다.")
    void Long_convert_long() {
        final Long l = Long.valueOf(1124L);
        final Class<?> clazz = WrapperToPrimitiveConverter.getPrimitiveClass(l.getClass());
        assertThat(clazz).isEqualTo(long.class);
    }

    @Test
    @DisplayName("래퍼 클래스가 아니면, 그대로 반환한다.")
    void object_not_convert() {
        final TestObject object = new TestObject("Just Testing");
        final Class<?> clazz = WrapperToPrimitiveConverter.getPrimitiveClass(object.getClass());
        assertThat(clazz).isEqualTo(object.getClass());
    }
}
