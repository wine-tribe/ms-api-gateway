package backend.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class AuditKafkaPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topic;

    public AuditKafkaPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${audit.topic:audit-topic}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topic = topic;
    }

    public void publish(AuditEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);

            // key = requestId (удобно для партиционирования/поиска)
            String key = (event.requestId() != null && !event.requestId().isBlank())
                    ? event.requestId()
                    : null;

            kafkaTemplate.send(topic, key, json);
        } catch (Exception ignored) {
            // аудит не должен ломать запросы
        }
    }
}