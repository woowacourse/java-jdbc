package nextstep.mvc.view;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ViewResolver {

    private static final Logger LOG = LoggerFactory.getLogger(ViewResolver.class);
    private static final String REDIRECT_PREFIX = "redirect:";

    private final Map<String, View> views = new ConcurrentHashMap<>();

    public View resolverViewName(String viewName) {
        return views.computeIfAbsent(viewName, key -> {
            if (viewName.startsWith(REDIRECT_PREFIX)) {
                LOG.info("Redirect View add : {}", viewName);
                return new RedirectView(viewName.substring(REDIRECT_PREFIX.length()));
            }
            if (viewName.endsWith(".jsp")) {
                LOG.info("Jsp View add : {}", viewName);
                return new JspView(viewName);
            }
            LOG.info("Json View add");
            return new JsonView();
        });
    }
}
