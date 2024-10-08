package com.interface21.webmvc.servlet.mvc.argumentresolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.interface21.web.bind.annotation.RequestParam;
import com.interface21.webmvc.servlet.mvc.MethodParameter;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestParamArgumentResolverTest {

    @DisplayName("RequestParam 어노테이션을 지원한다.")
    @Test
    void supportsTrue() {
        RequestParamArgumentResolver resolver = new RequestParamArgumentResolver();
        Method method = TestClass.class.getMethods()[0];
        MethodParameter methodParameter = new MethodParameter(method, 0);
        boolean supports = resolver.supports(methodParameter);

        assertThat(supports).isTrue();
    }

    @DisplayName("RequestParam 어노테이션이 없으면 지원하지 않는다.")
    @Test
    void supportsFalse() {
        RequestParamArgumentResolver resolver = new RequestParamArgumentResolver();
        Method method = TestClass.class.getMethods()[0];
        MethodParameter methodParameter = new MethodParameter(method, 1);
        boolean supports = resolver.supports(methodParameter);

        assertThat(supports).isFalse();
    }

    @DisplayName("RequestParam의 value를 키로하여 쿼리 파라미터를 반환한다.")
    @Test
    void resolveArgument() {
        RequestParamArgumentResolver resolver = new RequestParamArgumentResolver();
        Method method = TestClass.class.getMethods()[0];
        MethodParameter methodParameter = new MethodParameter(method, 0);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("account")).thenReturn("gugu");

        Object argument = resolver.resolveArgument(request, new MockHttpServletResponse(), methodParameter);

        assertThat(argument)
                .isEqualTo("gugu");
    }

    @DisplayName("RequestParam의 value에 해당하는 쿼리 파라미터가 없으면 안된다.")
    @Test
    void resolveArgumentNull() {
        RequestParamArgumentResolver resolver = new RequestParamArgumentResolver();
        Method method = TestClass.class.getMethods()[0];
        MethodParameter methodParameter = new MethodParameter(method, 0);

        assertThatThrownBy(
                () -> resolver.resolveArgument(new MockHttpServletRequest(), new MockHttpServletResponse(), methodParameter))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static class TestClass {

        public String test(
                @RequestParam("account") String account,
                String password
        ) {
            return "";
        }
    }
}
