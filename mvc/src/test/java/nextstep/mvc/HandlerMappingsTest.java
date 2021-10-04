package nextstep.mvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import nextstep.mvc.controller.AnnotationHandlerMapping;
import nextstep.mvc.controller.HandlerExecution;
import nextstep.mvc.exception.HandlerNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("HandlerMappings 일급 컬렉션은")
class HandlerMappingsTest {

    private HandlerMappings handlerMappings;

    @BeforeEach
    void setUp() {
        handlerMappings = new HandlerMappings();
    }

    @DisplayName("getHandler 요청시")
    @Nested
    class GetHandler {

        private HttpServletRequest request;

        @BeforeEach
        void setUp() {
            request = mock(HttpServletRequest.class);

            when(request.getRequestURI()).thenReturn("/get-test");
            when(request.getMethod()).thenReturn("GET");
        }

        @DisplayName("요청과 일치하는 Handler를 찾으면 반환한다.")
        @Test
        void getHandler() throws HandlerNotFoundException {
            // given
            AnnotationHandlerMapping handlerMapping = new AnnotationHandlerMapping("samples");

            // when
            handlerMappings.add(handlerMapping);
            handlerMapping.initialize();

            // then
            assertThat(handlerMappings.getHandler(request)).isExactlyInstanceOf(HandlerExecution.class);
        }

        @DisplayName("요청과 일치하는 Handler를 찾지 못할 경우 예외가 발생한다.")
        @Test
        void getHandlerException() {
            assertThatThrownBy(() -> handlerMappings.getHandler(request))
                .isExactlyInstanceOf(HandlerNotFoundException.class);
        }
    }
}