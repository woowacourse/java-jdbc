package webmvc.org.springframework.web.servlet.mvc.asis;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface Controller {

    String execute(HttpServletRequest req, HttpServletResponse res) throws Exception;
}
