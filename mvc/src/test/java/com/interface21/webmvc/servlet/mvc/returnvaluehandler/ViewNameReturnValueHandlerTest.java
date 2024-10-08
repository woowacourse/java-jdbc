package com.interface21.webmvc.servlet.mvc.returnvaluehandler;

import static org.assertj.core.api.Assertions.assertThat;

import com.interface21.webmvc.servlet.ModelAndView;
import com.interface21.webmvc.servlet.View;
import com.interface21.webmvc.servlet.view.JspView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ViewNameReturnValueHandlerTest {

    @DisplayName("String 타입을 지원한다.")
    @Test
    void supportsTrue() {
        ViewNameReturnValueHandler handler = new ViewNameReturnValueHandler();
        Object returnValue = new String("hi");
        boolean support = handler.supports(returnValue);

        assertThat(support).isTrue();
    }

    @DisplayName("String 타입이 아니면 지원하지 않는다.")
    @Test
    void supportsFalse() {
        ViewNameReturnValueHandler handler = new ViewNameReturnValueHandler();
        Object returnValue = Integer.parseInt("1");
        boolean support = handler.supports(returnValue);

        assertThat(support).isFalse();
    }

    @DisplayName("JspView인 ModelAndView로 변환한다.")
    @Test
    void handle() {
        ViewNameReturnValueHandler handler = new ViewNameReturnValueHandler();
        ModelAndView modelAndView = handler.handle("/index.jsp");

        View view = modelAndView.getView();

        assertThat(view).isInstanceOf(JspView.class);
    }
}
