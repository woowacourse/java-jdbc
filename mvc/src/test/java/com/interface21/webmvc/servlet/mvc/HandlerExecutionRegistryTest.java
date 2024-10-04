package com.interface21.webmvc.servlet.mvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.interface21.web.bind.annotation.RequestMethod;
import com.interface21.webmvc.servlet.HandlerExecution;
import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class HandlerExecutionRegistryTest {

    static class RegistryTestController {
        public void testHandler() {
        }
    }

    @Test
    @DisplayName("핸들러를 성공적으로 등록하고 조회한다.")
    void registerHandler() throws Exception {
        HandlerExecutionRegistry registry = new HandlerExecutionRegistry();
        Method handlerMethod = RegistryTestController.class.getMethod("testHandler");
        registry.registerHandler(new RequestMethod[]{RequestMethod.GET}, "/test", handlerMethod);

        HandlerExecution handlerExecution = registry.getHandler(RequestMethod.GET, "/test");
        assertThat(handlerExecution).isNotNull();
    }

    @Test
    @DisplayName("중복된 핸들러를 등록할 수 없다.")
    void duplicateHandler() throws Exception {
        HandlerExecutionRegistry registry = new HandlerExecutionRegistry();
        Method handlerMethod = RegistryTestController.class.getMethod("testHandler");
        RequestMethod[] requestMethods = {RequestMethod.GET};
        registry.registerHandler(requestMethods, "/test", handlerMethod);

        assertThatThrownBy(() -> registry.registerHandler(requestMethods, "/test", handlerMethod))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Handler already registered for");
    }

    @ParameterizedTest
    @EnumSource(RequestMethod.class)
    @DisplayName("빈 메서드 배열이 들어오는 경우, 모든 메서드에 대해 매핑한다.")
    void mapAllMethodOnNoParameterGiven(RequestMethod method) throws Exception {
        HandlerExecutionRegistry registry = new HandlerExecutionRegistry();
        Method handlerMethod = RegistryTestController.class.getMethod("testHandler");
        registry.registerHandler(new RequestMethod[]{}, "/test", handlerMethod);

        HandlerExecution handlerExecution = registry.getHandler(method, "/test");
        assertThat(handlerExecution).isNotNull();
    }
}
