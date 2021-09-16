package servlet;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

class ServletTest {

    /**
     * 2. 톰캣 maxThread 테스트 가능할까?
     *
     * 서블릿 컨테이너 설정 테스트 코드
     * accept-count 1
     * threads max 1
     */

    @Test
    void 서블릿_구현_테스트() throws LifecycleException, IOException {
        final Tomcat tomcat = createTomcat();
        final Context context = tomcat.addContext("", null);

        /**
         * 톰캣에 서블릿을 수동으로 추가한다.
         * 임베디드 톰캣을 직접 실행해서 서블릿 객체를 직접 주입했다.
         */
        Tomcat.addServlet(context, "HelloWorld", new HelloWorldServlet());
        context.addServletMappingDecoded("/hello-world", "HelloWorld");

        // 톰캣 띄우고
        tomcat.start();

        // 간단하게 HttpURLConnection을 사용해서 톰캣에 연결한다.
        final HttpURLConnection connection = connectTomcat("/hello-world");

        final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        final String actual = reader.readLine();

        // 서블릿 응답값 확인
        assertThat(actual).isEqualTo("Hello world");
    }

    private HttpURLConnection connectTomcat(String path) throws IOException {
        final URL url = new URL("http://localhost:8080" + path);
        return (HttpURLConnection) url.openConnection();
    }

    private Tomcat createTomcat() {
        final Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);
        skipBindOnInit(tomcat);
        return tomcat;
    }

    private void skipBindOnInit(Tomcat tomcat) {
        final Connector connector = tomcat.getConnector();
        connector.setProperty("bindOnInit", "false");
    }
}
