package dev.bootcamp.debtservice.infrastructure.messaging;

import dev.bootcamp.debtservice.domain.model.CustomerDebtStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DebtKafkaEventPublisherTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Mock private KafkaTemplate<String, String> kafkaTemplate;

    private DebtKafkaEventPublisher publisher;

    @BeforeEach
    void createPublisher() {
        Clock clock = Clock.fixed(Instant.parse("2026-12-01T10:00:00Z"), ZoneOffset.UTC);
        publisher = new DebtKafkaEventPublisher(kafkaTemplate, jsonMapper, clock, "debt-events");
    }

    @Test
    void publishesStatusChangedWithCustomerAsKey() {
        when(kafkaTemplate.send(eq("debt-events"), eq("c1"), anyString()))
                .thenReturn(CompletableFuture.completedFuture(null));

        publisher.publishStatusChanged(new CustomerDebtStatus("c1", new BigDecimal("300")))
                .test()
                .awaitDone(2, TimeUnit.SECONDS)
                .assertComplete();

        ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq("debt-events"), eq("c1"), json.capture());
        JsonNode event = jsonMapper.readTree(json.getValue());
        assertThat(event.get("eventType").asString()).isEqualTo("CustomerDebtStatusChanged");
        assertThat(event.get("eventId").asString()).hasSize(36);
        assertThat(event.get("hasOverdueDebt").asBoolean()).isTrue();
        assertThat(event.get("overdueAmount").decimalValue()).isEqualByComparingTo("300");
        assertThat(event.get("occurredAt").asString()).isEqualTo("2026-12-01T10:00:00Z");
    }

    @Test
    void failsWhenKafkaIsUnavailableSoTheStatusIsRetried() {
        when(kafkaTemplate.send(eq("debt-events"), eq("c1"), anyString()))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("down")));

        publisher.publishStatusChanged(new CustomerDebtStatus("c1", BigDecimal.ZERO))
                .test()
                .awaitDone(2, TimeUnit.SECONDS)
                .assertError(IllegalStateException.class);
    }
}
