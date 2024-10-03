package servlet.com.example;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * 서블릿(Servlet)은 웹 서버에서 실행되는 자바 프로그램이다.
 * 서블릿은 HTTP로 웹 클라이언트의 요청을 수신하고 응답한다.
 *
 * 자바 표준은 jakarta.servlet 패키지에서 Servlet 인터페이스를 제공한다.
 * Servlet 인터페이스의 service() 메서드는 HTTP 요청 및 응답을 처리할 때 사용된다.
 * 개발자는 service() 메서드에서 비즈니스 로직을 처리한다.
 * ```java
 * public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException;
 * ```
 *
 * 톰캣 만들기 미션의 RequestMapping 클래스는 아주 단순한 형태의 서블릿 컨테이너로 볼 수 있다.
 * RequestMapping 클래스에서 Controller 객체를 하나씩만 인스턴스화하여 url에 매핑한 것처럼
 * 실제 톰캣과 같은 서블릿 컨테이너도 서블릿 객체를 urlPatterns과 매핑한다.
 */
@WebServlet(name = "sharedCounterServlet", urlPatterns = "/shared-counter")
public class SharedCounterServlet extends HttpServlet {

    /**
     * ❗아래 변수는 문제가 있다.
     * 서블릿의 인스턴스 변수는 다른 스레드와 공유된다.
     * 서버는 여러 스레드에서 접근 가능하므로 서블릿에서 비즈니스 로직을 처리할 때 인스턴스 변수는 사용하지 않는다.
     * 다른 사용자에게 공유되어도 문제가 없는 불변 객체라면 서블릿의 인스턴스 변수로 사용 가능하다.
     */
    private Integer sharedCounter;

    @Override
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);
        getServletContext().log("init() 호출");
        sharedCounter = 0;
    }

    @Override
    protected void service(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        getServletContext().log("service() 호출");
        sharedCounter++;
        response.getWriter().write(String.valueOf(sharedCounter));
    }

    @Override
    public void destroy() {
        getServletContext().log("destroy() 호출");
    }
}
