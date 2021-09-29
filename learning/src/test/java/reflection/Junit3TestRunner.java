package reflection;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class Junit3TestRunner {

    @Test
    void run() throws Exception {
        Class<Junit3Test> clazz = Junit3Test.class;

        // TODO Junit3Test에서 test로 시작하는 메소드 실행
        Junit3Test junit3Test = clazz.getConstructor().newInstance();
        Method[] methods = clazz.getMethods();
        int count = 0;

        for (Method method : methods) {
            if (method.getName().startsWith("test")) {
                method.invoke(junit3Test);
                count++;
            }
        }

        int expectedCount = 2;

        assertThat(count).isEqualTo(expectedCount);
    }
}
