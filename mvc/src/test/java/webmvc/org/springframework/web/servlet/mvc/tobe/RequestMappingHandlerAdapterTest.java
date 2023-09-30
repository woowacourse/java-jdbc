package webmvc.org.springframework.web.servlet.mvc.tobe;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import web.org.springframework.web.bind.annotation.GetMapping;
import web.org.springframework.web.bind.annotation.RequestMapping;
import webmvc.org.springframework.web.servlet.ModelAndView;
import webmvc.org.springframework.web.servlet.View;
import webmvc.org.springframework.web.servlet.view.JspView;

import java.lang.reflect.Method;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class RequestMappingHandlerAdapterTest {

    private RequestMappingHandlerAdapter requestMappingHandlerAdapter;

    @BeforeEach
    void setup() {
        requestMappingHandlerAdapter = new RequestMappingHandlerAdapter();
    }

    @Test
    void 처리가_가능한_핸들러인지_판단할_수_있다() throws NoSuchMethodException {
        // given
        ClassCanHandle clazz = new ClassCanHandle();

        Method method = clazz.getClass()
                .getDeclaredMethod("get", HttpServletRequest.class, HttpServletResponse.class);

        // expect
        assertThat(requestMappingHandlerAdapter.supports(method)).isTrue();
    }

    @Test
    void 처리가_불가능한_핸들러인지_판단할_수_있다() throws NoSuchMethodException {
        // given
        ClassCannotHandle clazz = new ClassCannotHandle();

        Method method = clazz.getClass()
                .getDeclaredMethod("get", HttpServletRequest.class, HttpServletResponse.class);

        // expect
        assertThat(requestMappingHandlerAdapter.supports(method)).isFalse();
    }

    @Test
    void 핸들러를_실행시키고_결과를_반환할_수_있다() throws NoSuchMethodException {
        // given
        ClassCanHandle clazz = new ClassCanHandle();

        // when
        Method method = clazz.getClass()
                .getDeclaredMethod("get", HttpServletRequest.class, HttpServletResponse.class);
        HandlerExecution handlerExecution = new HandlerExecution(clazz, method);

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        // then
        ModelAndView modelAndView = requestMappingHandlerAdapter.handle(request, response, handlerExecution);

        assertThat(modelAndView).isNotNull();
    }

    @RequestMapping("/prefix")
    static class ClassCanHandle {

        @GetMapping("/get")
        public ModelAndView get(HttpServletRequest request, HttpServletResponse response) {
            View view = new JspView("viewName");
            return new ModelAndView(view);
        }
    }

    static class ClassCannotHandle {

        public ModelAndView get(HttpServletRequest request, HttpServletResponse response) {
            View view = new JspView("viewName");
            return new ModelAndView(view);
        }
    }
}
