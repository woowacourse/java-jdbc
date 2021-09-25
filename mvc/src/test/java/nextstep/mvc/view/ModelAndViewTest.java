package nextstep.mvc.view;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ModelAndView는")
class ModelAndViewTest {

    private ModelAndView modelAndView;

    @BeforeEach
    void setUp() {
        JspView jspView = new JspView("/index.jsp");
        modelAndView = new ModelAndView(jspView);
    }

    @DisplayName("addObject로 Model을 추가하고 getObject로 확인한다.")
    @Test
    void addAndGetObject() {
        // given
        String objectKey = "key";
        String objectValue = "value!";

        // when
        assertThat(modelAndView.getObject(objectKey)).isNull();

        ModelAndView newModelAndView = modelAndView.addObject(objectKey, objectValue);

        // then
        assertThat(modelAndView.getObject(objectKey)).isEqualTo(objectValue);
        assertThat(newModelAndView.getObject(objectKey)).isEqualTo(objectValue);
    }
}