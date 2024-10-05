package com.interface21.webmvc.servlet.mvc.tobe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.interface21.web.bind.annotation.RequestMethod;
import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.interface21.webmvc.servlet.mvc.sample.TestController;

class HandlerExecutionsTest {

    @DisplayName("핸들러를 성공적으로 등록하고 조회한다.")
    @Test
    void registerHandler() {
        HandlerExecutions handlerExecutions = new HandlerExecutions();
        Method[] methods = TestController.class.getDeclaredMethods();
        handlerExecutions.addHandlerExecution(methods);

        HandlerKey handlerKey = new HandlerKey("/test", RequestMethod.POST);
        HandlerKey notExist = new HandlerKey("/test", RequestMethod.GET);

        assertAll(
                () -> assertThat(handlerExecutions.containsHandlerKey(handlerKey)).isTrue(),
                () -> assertThat(handlerExecutions.containsHandlerKey(notExist)).isFalse(),
                () -> assertThat(handlerExecutions.get(handlerKey)).isNotNull(),
                () -> assertThat(handlerExecutions.get(notExist)).isNull()
        );
    }
}
