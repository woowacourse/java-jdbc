package reflection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class Junit4TestRunner extends JunitOutput {

    @DisplayName("Junit4Test에서 @MyTest 애노테이션이 있는 메소드 실행")
    @Test
    void run() throws Exception {
        Class<Junit4Test> clazz = Junit4Test.class;

        for (final Method method : clazz.getDeclaredMethods()) {
            if (Objects.nonNull(method.getAnnotation(MyTest.class))) {
                method.invoke(new Junit4Test());
            }
        }

        String result = outputStream.toString().trim();
        assertThat(result)
                .contains("Running Test1", "Running Test2")
                .doesNotContain("Running Test3");
    }
}
