package reflection;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class Junit4TestRunner extends JunitOutput {

    @Test
    void run() throws Exception {
        Class<Junit4Test> clazz = Junit4Test.class;
        final Junit4Test junit4Test = clazz.getConstructor().newInstance();

        final Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            final MyTest myTestAnnotation = method.getAnnotation(MyTest.class);
            if (!Objects.isNull(myTestAnnotation)) {
                method.invoke(junit4Test);
            }
        }

        String output = captor.toString().trim();

        assertThat(output)
                .contains("Running Test1")
                .contains("Running Test2")
                .doesNotContain("Running Test3");
    }
}
