package com.thiagsilvadev.erpapi.common.web.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestLoggingFilterTest {

    private final RequestLoggingFilter filter = new RequestLoggingFilter();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void shouldLogRequestWithoutCreatingCustomRequestId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/catalog/products");
        request.addHeader("X-Request-Id", "client-request-id");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean delegated = new AtomicBoolean();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            delegated.set(true);
            assertThat(MDC.get("requestId")).isNull();
        });

        assertThat(delegated).isTrue();
        assertThat(response.getHeader("X-Request-Id")).isNull();
        assertThat(MDC.get("requestId")).isNull();
    }

    @Test
    void shouldSkipHealthRequests() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health/readiness");

        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    @Test
    void shouldRunAfterServerObservationFilter() {
        Order order = RequestLoggingFilter.class.getAnnotation(Order.class);

        assertThat(order).isNotNull();
        assertThat(order.value()).isEqualTo(Ordered.HIGHEST_PRECEDENCE + 2);
    }
}
