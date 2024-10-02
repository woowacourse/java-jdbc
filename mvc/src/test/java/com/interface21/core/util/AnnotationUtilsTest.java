package com.interface21.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AnnotationUtilsTest {

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    private @interface TargetAnnotation {
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @TargetAnnotation
    private @interface MetaAnnotation {
    }

    @MetaAnnotation
    private static class Dummy {
    }

    @Test
    @DisplayName("메타 어노테이션을 통해 타겟 어노테이션을 찾아낸다.")
    void findMetaAnnotation() {
        boolean actual = AnnotationUtils.hasMetaAnnotatedClasses(Dummy.class, TargetAnnotation.class);
        assertThat(actual).isTrue();
    }


    // A -> B -> A의 순환 어노테이션

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @B
    @MetaAnnotation
    private @interface A {
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @A
    private @interface B {
    }

    @B
    private static class CyclicDummy {
    }

    @Test
    @DisplayName("순환 어노테이션이 발생하더라도 올바르게 찾아낸다.")
    void findOnCyclicAnnotatedClasses() {
        boolean actual = AnnotationUtils.hasMetaAnnotatedClasses(CyclicDummy.class, TargetAnnotation.class);
        assertThat(actual).isTrue();
    }
}
