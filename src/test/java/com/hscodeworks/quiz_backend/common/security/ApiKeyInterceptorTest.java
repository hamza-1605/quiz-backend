package com.hscodeworks.quiz_backend.common.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.hscodeworks.quiz_backend.common.exception.UnauthorizedException;

class ApiKeyInterceptorTest {

    private static final String KEY = "test-key";

    private final MockHttpServletResponse response = new MockHttpServletResponse();
    private final Object handler = new Object();

    @ParameterizedTest
    @ValueSource(strings = { "GET", "HEAD", "OPTIONS" })
    void readOnlyRequestsPassWithoutAKey(String method) {
        ApiKeyInterceptor interceptor = new ApiKeyInterceptor(KEY);

        assertThat(interceptor.preHandle(request(method, null), response, handler)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = { "POST", "PUT", "DELETE" })
    void writeRequestsWithoutAKeyAreRejected(String method) {
        ApiKeyInterceptor interceptor = new ApiKeyInterceptor(KEY);

        assertThatThrownBy(() -> interceptor.preHandle(request(method, null), response, handler))
                .isInstanceOf(UnauthorizedException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = { "POST", "PUT", "DELETE" })
    void writeRequestsWithAWrongKeyAreRejected(String method) {
        ApiKeyInterceptor interceptor = new ApiKeyInterceptor(KEY);

        assertThatThrownBy(() -> interceptor.preHandle(request(method, "wrong-key"), response, handler))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void keyComparisonIsCaseSensitive() {
        ApiKeyInterceptor interceptor = new ApiKeyInterceptor(KEY);

        assertThatThrownBy(() -> interceptor.preHandle(request("POST", "TEST-KEY"), response, handler))
                .isInstanceOf(UnauthorizedException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = { "POST", "PUT", "DELETE" })
    void writeRequestsWithTheCorrectKeyPass(String method) {
        ApiKeyInterceptor interceptor = new ApiKeyInterceptor(KEY);

        assertThat(interceptor.preHandle(request(method, KEY), response, handler)).isTrue();
    }

    @Test
    void whenNoKeyIsConfiguredAllWritesAreRejectedEvenWithAHeader() {
        ApiKeyInterceptor interceptor = new ApiKeyInterceptor("");

        assertThatThrownBy(() -> interceptor.preHandle(request("POST", "anything"), response, handler))
                .isInstanceOf(UnauthorizedException.class);
        assertThatThrownBy(() -> interceptor.preHandle(request("POST", ""), response, handler))
                .isInstanceOf(UnauthorizedException.class);
    }

    private static MockHttpServletRequest request(String method, String keyHeader) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, "/api/categories");
        if (keyHeader != null) {
            request.addHeader("X-API-Key", keyHeader);
        }
        return request;
    }
}