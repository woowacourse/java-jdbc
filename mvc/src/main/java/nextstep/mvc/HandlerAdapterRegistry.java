package nextstep.mvc;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import nextstep.mvc.handler.adapter.HandlerAdapter;

public class HandlerAdapterRegistry {

	private final List<HandlerAdapter> handlerAdapters;

	public HandlerAdapterRegistry(List<HandlerAdapter> handlerAdapters) {
		this.handlerAdapters = handlerAdapters;
	}

	public static HandlerAdapterRegistryBuilder builder() {
		return new HandlerAdapterRegistryBuilder();
	}

	public HandlerAdapter getAdapter(final Object handler) {
		return handlerAdapters.stream()
			.filter(adapter -> adapter.supports(handler))
			.findFirst()
			.orElseThrow(() -> new NoSuchElementException("해당 핸들러를 실행시킬 수 있는 어댑터가 없습니다."));
	}

	public static class HandlerAdapterRegistryBuilder {

		private final List<HandlerAdapter> handlerAdapters = new ArrayList<>();

		public HandlerAdapterRegistryBuilder add(HandlerAdapter handlerAdapter) {
			this.handlerAdapters.add(handlerAdapter);
			return this;
		}

		public HandlerAdapterRegistry build() {
			return new HandlerAdapterRegistry(handlerAdapters);
		}
	}
}
