package com.interface21.webmvc.servlet.mvc.returnvaluehandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.interface21.bean.container.BeanContainer;
import com.interface21.webmvc.servlet.ModelAndView;
import com.interface21.webmvc.servlet.View;
import com.interface21.webmvc.servlet.mvc.returnvaluehandler.JsonReturnValueHandlerTest.User;
import com.interface21.webmvc.servlet.view.JsonView;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReturnValueHandlersTest {

    @BeforeEach
    void setUp() {
        BeanContainer beanContainer = BeanContainer.getInstance();
        beanContainer.clear();
        beanContainer.registerBeans(List.of(new JsonReturnValueHandler(), new ModelAndViewReturnValueHandler(),
                new ViewNameReturnValueHandler()
        ));
    }

    @DisplayName("처리할 수 있는 ReturnValueHandler를 찾아 처리한다.")
    @Test
    void handle() {
        ReturnValueHandlers handlers = new ReturnValueHandlers();
        User user = new User("gugu", "account");

        ModelAndView modelAndView = handlers.handle(user);

        View view = modelAndView.getView();
        Map<String, Object> model = modelAndView.getModel();

        assertAll(
                () -> assertThat(view).isInstanceOf(JsonView.class),
                () -> assertThat(model).isEqualTo(Map.of("value", user))
        );
    }
}
