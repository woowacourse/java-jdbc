package samples;

import com.interface21.context.stereotype.Component;
import com.interface21.webmvc.servlet.mvc.HandlerExecution;
import com.interface21.webmvc.servlet.mvc.HandlerMapping;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

@Component
public class TestSuccessHandlerMappings  implements HandlerMapping {

    @Override
    public void initialize() {
    }

    @Override
    public Object getHandler(HttpServletRequest request) {
        Method method = TestController.class.getMethods()[0];
        return new HandlerExecution(method);
    }
}
