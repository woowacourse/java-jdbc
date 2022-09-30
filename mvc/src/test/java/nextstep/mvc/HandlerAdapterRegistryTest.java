package nextstep.mvc;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import nextstep.mvc.handler.HandlerExecution;
import nextstep.mvc.handler.adapter.AnnotationHandlerAdapter;
import nextstep.mvc.handler.adapter.HandlerAdapter;

class HandlerAdapterRegistryTest {

	@DisplayName("어노테이션 기반 어댑터를 찾는다.")
	@Test
	void getAdapter() {
		// given
		final var handlerAdapterRegistry = HandlerAdapterRegistry.builder()
			.add(new AnnotationHandlerAdapter())
			.build();
		Object handler = mock(HandlerExecution.class);

		// when
		HandlerAdapter adapter = handlerAdapterRegistry.getAdapter(handler);

		// then
		assertThat(adapter).isInstanceOf(AnnotationHandlerAdapter.class);
	}
}
