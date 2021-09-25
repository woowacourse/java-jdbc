package nextstep.mvc.controller;

import static org.assertj.core.api.Assertions.assertThat;

import nextstep.mvc.controller.HandlerKey;
import nextstep.web.support.RequestMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("HandlerKey는")
class HandlerKeyTest {

    @DisplayName("다른 HandlerKey과 url이 같을 때")
    @Nested
    class SameUrl {

        private static final String URL = "wow";

        @DisplayName("RequestMethod까지 같다면 서로 같다.")
        @Test
        void sameRequestMethod() {
            // given
            HandlerKey handlerKey1 = new HandlerKey(URL, RequestMethod.GET);
            HandlerKey handlerKey2 = new HandlerKey(URL, RequestMethod.GET);

            // when, then
            assertThat(handlerKey1).isEqualTo(handlerKey2);
        }

        @DisplayName("RequestMethod가 다르다면 서로 다르다.")
        @Test
        void differentRequestMethod() {
            // given
            HandlerKey handlerKey1 = new HandlerKey(URL, RequestMethod.GET);
            HandlerKey handlerKey2 = new HandlerKey(URL, RequestMethod.POST);

            // when, then
            assertThat(handlerKey1).isNotEqualTo(handlerKey2);
        }
    }

    @DisplayName("다른 HandlerKey과 url이 다를 때")
    @Nested
    class DifferentUrl {

        @DisplayName("RequestMethod가 같아도 서로 다르다.")
        @Test
        void sameRequestMethod() {
            // given
            HandlerKey handlerKey1 = new HandlerKey("wow", RequestMethod.GET);
            HandlerKey handlerKey2 = new HandlerKey("toc", RequestMethod.GET);

            // when, then
            assertThat(handlerKey1).isNotEqualTo(handlerKey2);
        }

        @DisplayName("RequestMethod가 다르다면 서로 다르다.")
        @Test
        void differentRequestMethod() {
            // given
            HandlerKey handlerKey1 = new HandlerKey("wow", RequestMethod.GET);
            HandlerKey handlerKey2 = new HandlerKey("toc", RequestMethod.POST);

            // when, then
            assertThat(handlerKey1).isNotEqualTo(handlerKey2);
        }
    }
}