package com.interface21.webmvc.servlet.mvc.tobe;

import static org.assertj.core.api.Assertions.assertThat;

import com.interface21.web.bind.annotation.RequestMethod;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.interface21.webmvc.servlet.mvc.sample.TestController;

class HandlerKeyExtractorTest {

    @DisplayName("메소드의 RequestMapping을 통해 HandlerKey 리스트를 생성한다")
    @Test
    void extract() throws Exception {
        Method method = TestController.class.getDeclaredMethod(
                "test",
                HttpServletRequest.class,
                HttpServletResponse.class
        );
        List<HandlerKey> handlerKeys = HandlerKeyExtractor.extract(method);

        List<HandlerKey> expected = List.of(new HandlerKey("/test", RequestMethod.POST));

        assertThat(handlerKeys).isEqualTo(expected);
    }

    @DisplayName("method를 지정하지 않은 경우 모든 RequestMethod에 대해 HandlerKey를 생성한다")
    @Test
    void notExistMethod() throws Exception {
        Method method = TestController.class.getDeclaredMethod(
                "notExistMethod",
                HttpServletRequest.class,
                HttpServletResponse.class
        );
        List<HandlerKey> handlerKeys = HandlerKeyExtractor.extract(method);

        assertThat(handlerKeys).hasSize(RequestMethod.values().length);
    }
}
