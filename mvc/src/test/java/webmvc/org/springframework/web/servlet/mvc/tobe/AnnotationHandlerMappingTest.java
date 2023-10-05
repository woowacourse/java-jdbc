package webmvc.org.springframework.web.servlet.mvc.tobe;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import webmvc.org.springframework.web.servlet.ModelAndView;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class AnnotationHandlerMappingTest {

    private AnnotationHandlerMapping handlerMapping;
    private AnnotationHandlerAdapter handlerAdapter;
    private DefaultHandlerAdapter defaultHandlerAdapter;

    @BeforeEach
    void setUp() throws NoSuchMethodException, InstantiationException, IllegalAccessException {
        handlerMapping = new AnnotationHandlerMapping("samples");
        handlerMapping.initialize();
        handlerAdapter = new AnnotationHandlerAdapter();
        defaultHandlerAdapter = new DefaultHandlerAdapter();
    }

    @Test
    @DisplayName("요청 메서드가 GET이고 경로가 /get-test이면 해당 값이 @RequestMapping으로 매핑된 핸들러가 호출된다.")
    void getHandler_get() throws Exception {
        // given
        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);

        given(request.getAttribute("id")).willReturn("gugu");
        given(request.getRequestURI()).willReturn("/get-test");
        given(request.getMethod()).willReturn("GET");

        // when
        final Handler handler = handlerMapping.getHandler(request);
        final ModelAndView modelAndView = handlerAdapter.handle(request, response, handler);

        // then
        assertThat(modelAndView.getObject("id")).isEqualTo("gugu");
    }

    @Test
    @DisplayName("요청 메서드가 POST이고 경로가 /post-test이면 해당 값이 @RequestMapping으로 매핑된 핸들러가 호출된다.")
    void getHandler_post() throws Exception {
        // given
        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);

        given(request.getAttribute("id")).willReturn("gugu");
        given(request.getRequestURI()).willReturn("/post-test");
        given(request.getMethod()).willReturn("POST");

        // when
        final Handler handler = handlerMapping.getHandler(request);
        final var modelAndView = handlerAdapter.handle(request, response, handler);

        //then
        assertThat(modelAndView.getObject("id")).isEqualTo("gugu");
    }
}
