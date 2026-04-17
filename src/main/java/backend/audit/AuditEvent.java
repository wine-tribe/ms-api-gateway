package backend.audit;

import java.time.Instant;

public record AuditEvent(
        Instant ts,
        String requestId,
        String routeId,
        String method,
        String host,
        String path,
        String query,
        Integer status,
        long durationMs,
        String userName,
        String clientIp,
        String userAgent
) {
}