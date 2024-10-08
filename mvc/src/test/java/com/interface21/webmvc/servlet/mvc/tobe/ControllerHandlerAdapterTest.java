package com.interface21.webmvc.servlet.mvc.tobe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.interface21.webmvc.servlet.ModelAndView;
import com.interface21.webmvc.servlet.View;
import com.interface21.webmvc.servlet.mvc.Controller;
import com.interface21.webmvc.servlet.mvc.HandlerAdapter;
import com.interface21.webmvc.servlet.mvc.handleradaptor.ControllerHandlerAdapter;
import com.interface21.webmvc.servlet.view.JspView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class ControllerHandlerAdapterTest {

    @DisplayName("Controller의 반환값 String을 이름으로 하는 JspView로 변환시킨다.")
    @Test
    void test() throws Exception {
        HandlerAdapter adaptor = new ControllerHandlerAdapter();
        Controller controller = (request, response) -> "aaa";

        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();

        ModelAndView modelAndView = adaptor.handle(request, response, controller);

        View view = modelAndView.getView();
        assertAll(
                () -> assertThat(view).isInstanceOf(JspView.class),
                () -> assertThat(((JspView) view)).extracting("viewName").isEqualTo("aaa")
        );

    }

    @DisplayName("Controller 인터페이스 구현체를 지원한다.")
    @Test
    void supportsTrue() {
        Object testController = new TestController();

        ControllerHandlerAdapter handlerAdapter = new ControllerHandlerAdapter();
        boolean support = handlerAdapter.supports(testController);

        assertThat(support).isTrue();
    }

    @DisplayName("Controller 인터페이스 구현체가 아니면 지원하지 않는다.")
    @Test
    void supportsFalse() {
        Object testController = new Object();

        ControllerHandlerAdapter handlerAdapter = new ControllerHandlerAdapter();
        boolean support = handlerAdapter.supports(testController);

        assertThat(support).isFalse();
    }

    private static class TestController implements Controller {
        @Override
        public String execute(HttpServletRequest req, HttpServletResponse res) throws Exception {
            return "";
        }
    }
}
