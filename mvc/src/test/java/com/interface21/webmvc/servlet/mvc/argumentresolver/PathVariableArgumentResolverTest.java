package com.interface21.webmvc.servlet.mvc.argumentresolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.interface21.web.bind.annotation.PathVariable;
import com.interface21.web.bind.annotation.RequestMapping;
import com.interface21.webmvc.servlet.mvc.MethodParameter;
import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class PathVariableArgumentResolverTest {

    @DisplayName("PathVariable 어노테이션과 RequestMapping 어노테이션이 붙어 있어야 지원한다.")
    @Test
    void supportsTrue() {
        PathVariableArgumentResolver argumentResolver = new PathVariableArgumentResolver();
        Method method = TestClass.class.getMethods()[0];
        MethodParameter methodParameter = new MethodParameter(method, 0);
        boolean supports = argumentResolver.supports(methodParameter);

        assertThat(supports).isTrue();
    }

    @DisplayName("PathVariable 어노테이션이 없으면 지원하지 않는다.")
    @Test
    void supportsFalse() {
        PathVariableArgumentResolver argumentResolver = new PathVariableArgumentResolver();
        Method method = TestClass.class.getMethods()[1];
        MethodParameter methodParameter = new MethodParameter(method, 0);
        boolean supports = argumentResolver.supports(methodParameter);

        assertThat(supports).isFalse();
    }

    @DisplayName("PathValue에 해당하는 값을 URI에서 찾는다.")
    @Test
    void resolveArgument() {
        PathVariableArgumentResolver argumentResolver = new PathVariableArgumentResolver();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/events/1/bills/2");
        MockHttpServletResponse response = new MockHttpServletResponse();
        Method method = TestClass.class.getMethods()[0];
        MethodParameter methodParameter1 = new MethodParameter(method, 0);
        MethodParameter methodParameter2 = new MethodParameter(method, 1);

        Object argument1 = argumentResolver.resolveArgument(request, response, methodParameter1);
        Object argument2 = argumentResolver.resolveArgument(request, response, methodParameter2);

        assertAll(
                () -> assertThat(argument1).isEqualTo("1"),
                () -> assertThat(argument2).isEqualTo(2L)
        );
    }

    private static class TestClass {

        @RequestMapping("/api/events/{eventId}/bills/{billId}")
        public String test1(
                @PathVariable("eventId") String eventId,
                @PathVariable("billId") Long billId
        ) {
            return "";
        }

        @RequestMapping("/api/events/{eventId}/bills/{billId}")
        public String test2(
                String eventId,
                @PathVariable("password") String password
        ) {
            return "";
        }
    }
}
