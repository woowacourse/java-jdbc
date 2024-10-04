package com.interface21.webmvc.servlet.mvc.argumentresolver;

import static org.assertj.core.api.Assertions.assertThat;

import com.interface21.webmvc.servlet.mvc.MethodParameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DefaultResponseArgumentResolverTest {

    @DisplayName("HttpServletResponse를 지원한다.")
    @Test
    void supportsTrue() {
        DefaultResponseArgumentResolver resolver = new DefaultResponseArgumentResolver();
        Method method = TestClass.class.getMethods()[0];
        MethodParameter methodParameter = new MethodParameter(method, 1);
        boolean supports = resolver.supports(methodParameter);

        assertThat(supports).isTrue();
    }

    @DisplayName("HttpServletResponse가 아니면 지원하지 않는다.")
    @Test
    void supportsFalse() {
        DefaultResponseArgumentResolver resolver = new DefaultResponseArgumentResolver();
        Method method = TestClass.class.getMethods()[0];
        MethodParameter methodParameter = new MethodParameter(method, 0);
        boolean supports = resolver.supports(methodParameter);

        assertThat(supports).isFalse();
    }

    private static class TestClass {

        public String test(HttpServletRequest request, HttpServletResponse response) {
            return "";
        }
    }
}
