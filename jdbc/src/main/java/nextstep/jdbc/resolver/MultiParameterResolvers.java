package nextstep.jdbc.resolver;

import java.util.Arrays;
import java.util.List;

public class MultiParameterResolvers {

    private final List<MultiParameterResolver> multiParameterResolvers;

    public MultiParameterResolvers(MultiParameterResolver... multiParameterResolver) {
        this.multiParameterResolvers = Arrays.asList(multiParameterResolver);
    }

    public <T> MultiParameterResolver findProperResolver(T data) {
        return multiParameterResolvers
            .stream()
            .filter(multiParameterResolver -> multiParameterResolver.support(data))
            .findAny()
            .orElseThrow(() -> new IllegalStateException(
                String.format("해당하는 타입의 ParameterResolver가 없습니다. type => %s",
                    data.getClass().getSimpleName())));
    }
}
