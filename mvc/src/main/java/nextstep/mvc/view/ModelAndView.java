package nextstep.mvc.view;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ModelAndView {

    private final View view;
    private final Map<String, Object> model;

    public ModelAndView(final View view) {
        this.view = view;
        this.model = new HashMap<>();
    }

    public void addObject(final String attributeName, final Object attributeValue) {
        model.put(attributeName, attributeValue);
    }

    public Object getObject(final String attributeName) {
        return model.get(attributeName);
    }

    public View getView() {
        return view;
    }

    public void render(HttpServletRequest request, HttpServletResponse response) throws Exception {
        view.render(model, request, response);
    }
}
