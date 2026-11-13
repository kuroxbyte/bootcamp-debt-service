package dev.bootcamp.debtservice.infrastructure.messaging;

import dev.bootcamp.debtservice.application.port.DebtEventPublisher;
import dev.bootcamp.debtservice.domain.model.CustomerDebtStatus;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Implementa el puerto {@link DebtEventPublisher} publicando {@code CustomerDebtStatusChanged} en
 * el tópico {@code debt-events}. La clave de cada mensaje es el id del cliente. El contrato está en
 * {@code docs/events/asyncapi.yaml} del repositorio raíz.
 *
 * <p>A diferencia de los demás publicadores, un error no se descarta: termina con error para que
 * el estado no se dé por publicado y se vuelva a intentar.
 */
@Slf4j
@RequiredArgsConstructor
public class DebtKafkaEventPublisher implements DebtEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JsonMapper jsonMapper;
    private final Clock clock;
    private final String topic;

    @Override
    public Completable publishStatusChanged(CustomerDebtStatus status) {
        return Completable.defer(() -> {
            Map<String, Object> event = new LinkedHashMap<>();
            event.put("eventType", "CustomerDebtStatusChanged");
            event.put("eventId", UUID.randomUUID().toString());
            event.put("customerId", status.customerId());
            event.put("hasOverdueDebt", status.hasOverdueDebt());
            event.put("overdueAmount", status.overdueAmount());
            event.put("occurredAt", Instant.now(clock).toString());
            return Completable.fromCompletionStage(kafkaTemplate.send(
                    topic,
                    status.customerId(),
                    jsonMapper.writeValueAsString(event)));
        })
                // send() puede bloquear mientras busca al broker: fuera del hilo que lo llama.
                .subscribeOn(Schedulers.io())
                .doOnComplete(() -> log.info(
                        "CustomerDebtStatusChanged published customerId={} hasOverdueDebt={}"
                                + " overdueAmount={}",
                        status.customerId(),
                        status.hasOverdueDebt(),
                        status.overdueAmount()));
    }
}
