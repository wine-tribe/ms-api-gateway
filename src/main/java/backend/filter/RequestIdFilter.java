package backend.filter;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class RequestIdFilter implements GlobalFilter, Ordered {

    public static final String HDR = "X-Request-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        String rid = exchange.getRequest().getHeaders().getFirst(HDR);
        if (rid == null || rid.isBlank()) rid = UUID.randomUUID().toString();

        String finalRid = rid;
        ServerHttpRequest mutated = exchange.getRequest().mutate()
                .headers(h -> {
                    h.remove(HDR);
                    h.add(HDR, finalRid);
                })
                .build();

        // вернём requestId клиенту
        exchange.getResponse().getHeaders().set(HDR, rid);

        return chain.filter(exchange.mutate().request(mutated).build());
    }

    @Override public int getOrder() { return Ordered.HIGHEST_PRECEDENCE; }
}