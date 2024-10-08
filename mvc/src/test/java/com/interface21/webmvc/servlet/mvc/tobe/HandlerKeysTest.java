package com.interface21.webmvc.servlet.mvc.tobe;

import static org.assertj.core.api.Assertions.assertThat;

import com.interface21.web.bind.annotation.RequestMapping;
import com.interface21.web.bind.annotation.RequestMethod;
import com.interface21.webmvc.servlet.mvc.HandlerKey;
import com.interface21.webmvc.servlet.mvc.HandlerKeys;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HandlerKeysTest {

    @DisplayName("RequestMapping으로 HandlerKeys를 생성한다.")
    @Test
    void from() throws NoSuchMethodException {
        Method method = HandlerKeysTest.class.getDeclaredMethod("requestMappingTest");
        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);

        HandlerKeys handlerKeys = HandlerKeys.from(requestMapping);

        List<HandlerKey> keys = handlerKeys.getKeys();
        assertThat(keys).hasSize(2)
                .containsExactlyInAnyOrder(
                        new HandlerKey("/login", RequestMethod.GET),
                        new HandlerKey("/login", RequestMethod.POST)
                );
    }

    @RequestMapping(value = "/login", method = {RequestMethod.GET, RequestMethod.POST})
    private void requestMappingTest() {
    }

    @DisplayName("RequestMapping에 method에 없는 경우 모든 method에 대해 HandlerKeys를 생성한다.")
    @Test
    void fromNoMethod() throws NoSuchMethodException {
        Method method = HandlerKeysTest.class.getDeclaredMethod("requestMappingNoMethodTest");
        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);

        HandlerKeys handlerKeys = HandlerKeys.from(requestMapping);

        List<HandlerKey> keys = handlerKeys.getKeys();
        assertThat(keys).hasSize(8)
                .containsExactlyInAnyOrder(
                        new HandlerKey("/login", RequestMethod.GET),
                        new HandlerKey("/login", RequestMethod.POST),
                        new HandlerKey("/login", RequestMethod.PATCH),
                        new HandlerKey("/login", RequestMethod.PUT),
                        new HandlerKey("/login", RequestMethod.DELETE),
                        new HandlerKey("/login", RequestMethod.HEAD),
                        new HandlerKey("/login", RequestMethod.OPTIONS),
                        new HandlerKey("/login", RequestMethod.TRACE)
                );
    }

    @RequestMapping(value = "/login")
    private void requestMappingNoMethodTest() {
    }
}
