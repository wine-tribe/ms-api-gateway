package backend.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Component
public class RequestIdLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestIdLoggingFilter.class);

    public static final String HEADER_REQUEST_ID = "X-Request-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestId = exchange.getRequest().getHeaders().getFirst(HEADER_REQUEST_ID);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }

        Instant start = Instant.now();

        String finalRequestId = requestId;
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .headers(h -> {
                    h.remove(HEADER_REQUEST_ID);
                    h.add(HEADER_REQUEST_ID, finalRequestId);
                })
                .build();

        // Отдаём requestId обратно клиенту
        exchange.getResponse().getHeaders().set(HEADER_REQUEST_ID, requestId);

        return chain.filter(exchange.mutate().request(mutatedRequest).build())
                .doFinally(signalType -> {
                    var method = exchange.getRequest().getMethod();
                    var path = exchange.getRequest().getURI().getRawPath();
                    var status = exchange.getResponse().getStatusCode() != null
                            ? exchange.getResponse().getStatusCode().value()
                            : 0;
                    long ms = Duration.between(start, Instant.now()).toMillis();

                    // access-log на уровне gateway (то, что ты дальше превратишь в аудит/журналирование)
                    log.info("requestId={} method={} path={} status={} durationMs={}",
                            finalRequestId,
                            method != null ? method.name() : "UNKNOWN",
                            path,
                            status,
                            ms
                    );
                });
    }

    @Override
    public int getOrder() {
        // Чем меньше — тем раньше выполнится фильтр.
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
