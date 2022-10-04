package nextstep.mvc;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import nextstep.mvc.handler.mapping.HandlerMapping;

public class HandlerMappingRegistry {

	private final List<HandlerMapping> handlerMappings;

	public HandlerMappingRegistry(List<HandlerMapping> handlerMappings) {
		this.handlerMappings = handlerMappings;
	}

	public static HandlerMappingRegistryBuilder builder() {
		return new HandlerMappingRegistryBuilder();
	}

	public void initialize() {
		handlerMappings.forEach(HandlerMapping::initialize);
	}

	public Object getHandler(final HttpServletRequest request) {
		return handlerMappings.stream()
			.map(handlerMapping -> handlerMapping.getHandler(request))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.findFirst()
			.orElseThrow(() -> new NoSuchElementException("매핑되는 핸들러가 없습니다."));
	}

	public static class HandlerMappingRegistryBuilder {

		private final List<HandlerMapping> handlerMappings = new ArrayList<>();

		public HandlerMappingRegistryBuilder add(HandlerMapping handlerMapping) {
			this.handlerMappings.add(handlerMapping);
			return this;
		}

		public HandlerMappingRegistry build() {
			return new HandlerMappingRegistry(handlerMappings);
		}
	}
}
