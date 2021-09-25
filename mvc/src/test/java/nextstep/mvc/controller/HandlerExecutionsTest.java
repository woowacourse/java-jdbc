package nextstep.mvc.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import nextstep.mvc.controller.HandlerExecution;
import nextstep.mvc.controller.HandlerExecutions;
import nextstep.web.annotation.Controller;
import nextstep.web.support.RequestMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;

@DisplayName("HandlerExecutions 일급 컬렉션은")
class HandlerExecutionsTest {

    private HandlerExecutions handlerExecutions;

    @BeforeEach
    void setUp() throws Exception {
        Reflections reflections = new Reflections("samples");
        Set<Class<?>> annotatedHandlers = reflections.getTypesAnnotatedWith(Controller.class);

        handlerExecutions = new HandlerExecutions();
        handlerExecutions.initializeWith(annotatedHandlers);
    }

    @DisplayName("getHandlerExecution 조회에 URI가 일치하는 Handler가 있을 때")
    @Nested
    class MatchURI {

        private static final String URI = "/get-test";

        @DisplayName("RequestMethod도 일치하면 HandlerExecution을 반환한다.")
        @Test
        void getHandlerExecution() {
            // when
            Object handlerExecution = handlerExecutions.getHandlerExecution(URI, RequestMethod.GET);

            // then
            assertThat(handlerExecution).isExactlyInstanceOf(HandlerExecution.class);
        }

        @DisplayName("RequestMethod가 일치하지 않으면 null을 반환한다.")
        @Test
        void getHandlerExecutionNull() {
            // when
            Object handlerExecution = handlerExecutions.getHandlerExecution(URI, RequestMethod.POST);

            // then
            assertThat(handlerExecution).isNull();
        }
    }

    @DisplayName("getHandlerExecution 조회에 URI가 일치하는 Handler가 없을 때")
    @Nested
    class NonMatchURI {

        @DisplayName("RequestMethod가 일치해도 null을 반환한다.")
        @Test
        void getHandlerExecution() {
            // when
            Object handlerExecution = handlerExecutions.getHandlerExecution("*(*#@&$", RequestMethod.GET);

            // then
            assertThat(handlerExecution).isNull();
        }

        @DisplayName("RequestMethod가 일치하지 않으면 null을 반환한다.")
        @Test
        void getHandlerExecutionNull() {
            // when
            Object handlerExecution = handlerExecutions.getHandlerExecution("*(*#@&$", RequestMethod.DELETE);

            // then
            assertThat(handlerExecution).isNull();
        }
    }
}
