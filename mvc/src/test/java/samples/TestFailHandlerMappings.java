package samples;

import com.interface21.context.stereotype.Component;
import com.interface21.webmvc.servlet.mvc.HandlerMapping;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class TestFailHandlerMappings  implements HandlerMapping {

    @Override
    public void initialize() {
    }

    @Override
    public Object getHandler(HttpServletRequest request) {
        throw new IllegalArgumentException();
    }
}
