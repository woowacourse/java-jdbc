package com.interface21.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

import com.interface21.context.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class BeanRegistrarTest {

    @Component
    static class Dummy {
        public Dummy() {
        }
    }

    static class DummyWithoutAnnotation {
        public DummyWithoutAnnotation() {
        }
    }

    MockedStatic<BeanContainerFactory> factory;

    @BeforeEach
    void setUp() {
        factory = mockStatic(BeanContainerFactory.class);
    }

    @AfterEach
    void tearDown() {
        factory.close();
    }


    @Test
    @DisplayName("Component 어노테이션의 빈을 등록한다.")
    void
    registerBeans() {
        Map<Class<?>, Object> beans = new HashMap<>();
        factory.when(BeanContainerFactory::getContainer)
                .thenReturn(new FakeBeanContainer(beans));

        BeanRegistrar.registerBeans(getClass());
        assertThat(beans).containsKey(Dummy.class);

    }

    @Test
    @DisplayName("Component 어노테이션이 없는 클래스는 빈으로 등록하지 않는다.")
    void registerBeansWithoutAnnotation() {
        Map<Class<?>, Object> beans = new HashMap<>();
        factory.when(BeanContainerFactory::getContainer)
                .thenReturn(new FakeBeanContainer(beans));

        BeanRegistrar.registerBeans(getClass());
        assertThat(beans).doesNotContainKey(DummyWithoutAnnotation.class);
    }
}
