package reflection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class Junit3TestRunner extends JunitOutput {

    @DisplayName("Junit3Test에서 test로 시작하는 메소드 실행")
    @Test
    void run() throws Exception {
        Class<Junit3Test> clazz = Junit3Test.class;

        for (final Method method : clazz.getDeclaredMethods()) {
            if (method.getName().startsWith("test")) {
                method.invoke(new Junit3Test());
            }
        }

        String result = outputStream.toString().trim();
        assertThat(result)
                .contains("Running Test1", "Running Test2")
                .doesNotContain("Running Test3");
    }
}
