package nextstep.mvc.view.util;

import nextstep.mvc.view.JspView;
import nextstep.mvc.view.ModelAndView;

public class ViewRedirectHelper {

    public static ModelAndView redirect(String path) {
        return new ModelAndView(new JspView(JspView.REDIRECT_PREFIX + path));
    }
}
