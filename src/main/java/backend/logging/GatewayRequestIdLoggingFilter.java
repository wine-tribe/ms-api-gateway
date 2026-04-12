package backend.logging;

import backend.filter.RequestIdLoggingFilter;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;

@Component
public class GatewayRequestIdLoggingFilter implements GlobalFilter, Ordered {

    public static final String HEADER = "X-Request-Id";
    public static final String MDC_KEY = "requestId";
    private static final Logger logger = LoggerFactory.getLogger(RequestIdLoggingFilter.class);

    @Override
    public int getOrder() {
        return -100; // пораньше
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        long start = System.currentTimeMillis();

        String requestId = Optional.ofNullable(exchange.getRequest().getHeaders().getFirst(HEADER))
                .filter(s -> !s.isBlank())
                .orElse(UUID.randomUUID().toString());

        // прокидываем заголовок дальше в микросервисы
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header(HEADER, requestId)
                .build();

        exchange.getResponse().getHeaders().set(HEADER, requestId);

        // MDC в реактиве: самый простой “базовый” вариант — поставить на входе и убрать на завершении
        MDC.put(MDC_KEY, requestId);

        return chain.filter(exchange.mutate().request(mutatedRequest).build())
                .doFinally(signal -> {
                    long tookMs = System.currentTimeMillis() - start;
                    int status = exchange.getResponse().getStatusCode() != null
                            ? exchange.getResponse().getStatusCode().value()
                            : 0;

                    logger.info("GATEWAY {} {} -> {} ({} ms)",
                            exchange.getRequest().getMethod(),
                            exchange.getRequest().getURI().getPath(),
                            status,
                            tookMs);

                    MDC.remove(MDC_KEY);
                });
    }
}