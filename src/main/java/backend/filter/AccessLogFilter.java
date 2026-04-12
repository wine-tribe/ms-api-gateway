package backend.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

@Component
public class AccessLogFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(AccessLogFilter.class);

    @Override
    public reactor.core.publisher.Mono<Void> filter(
            ServerWebExchange exchange,
            org.springframework.cloud.gateway.filter.GatewayFilterChain chain
    ) {
        long start = System.nanoTime();

        String rid = exchange.getRequest().getHeaders().getFirst(RequestIdFilter.HDR);

        String method = exchange.getRequest().getMethod() != null
                ? exchange.getRequest().getMethod().name()
                : "UNKNOWN";

        String path = exchange.getRequest().getURI().getPath();

        return chain.filter(exchange)
                .doFinally(__ -> {
                    long durationMs = (System.nanoTime() - start) / 1_000_000;
                    HttpStatusCode st = exchange.getResponse().getStatusCode();
                    int status = st != null ? st.value() : 0;

                    log.info("requestId={} method={} path={} status={} durationMs={}",
                            rid, method, path, status, durationMs);
                });
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
