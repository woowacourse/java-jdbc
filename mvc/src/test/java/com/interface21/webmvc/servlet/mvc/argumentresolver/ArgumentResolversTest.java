package com.interface21.webmvc.servlet.mvc.argumentresolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.interface21.bean.container.BeanContainer;
import com.interface21.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

class ArgumentResolversTest {

    @DisplayName("파라미터에 맞는 Arguments를 생성한다.")
    @Test
    void handle() {
        BeanContainer beanContainer = BeanContainer.getInstance();
        beanContainer.registerBeans(List.of(new RequestParamArgumentResolver(), new DefaultRequestArgumentResolver(),
                new DefaultResponseArgumentResolver()
        ));
        ArgumentResolvers argumentResolvers = new ArgumentResolvers();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("account")).thenReturn("gugu");
        MockHttpServletResponse response = new MockHttpServletResponse();
        Method method = TestClass.class.getMethods()[0];

        Object[] args = argumentResolvers.handle(request, response, method);

        assertAll(
                () -> assertThat(args).hasSize(2),
                () -> assertThat(args[0]).isEqualTo("gugu"),
                () -> assertThat(args[1]).isEqualTo(request)
        );
    }

    private static class TestClass {

        public String test(@RequestParam("account") String account, HttpServletRequest request) {
            return "";
        }
    }
}
