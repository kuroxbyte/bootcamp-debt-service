package dev.bootcamp.debtservice.infrastructure.messaging;

import dev.bootcamp.debtservice.application.usecase.RecordDebtEventUseCase;
import dev.bootcamp.debtservice.domain.model.DebtMovement;
import dev.bootcamp.debtservice.domain.model.DebtMovementType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.util.Map;

/**
 * Consume el tópico {@code transaction-events} de transaction-service: pagos a créditos y
 * tarjetas, y consumos de tarjetas. El contrato está en {@code docs/events/asyncapi.yaml} del
 * repositorio raíz.
 *
 * <p>Ignora los tipos de evento que no conoce. Si registrar falla, la excepción hace que Kafka
 * vuelva a entregar el mensaje; un evento repetido no se cuenta dos veces (su id es único).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionEventListener {

    private static final Map<String, DebtMovementType> TYPES = Map.of(
            "CreditPaymentRegistered", DebtMovementType.CREDIT_PAYMENT,
            "CreditCardPaymentRegistered", DebtMovementType.CREDIT_CARD_PAYMENT,
            "CreditCardChargeRegistered", DebtMovementType.CREDIT_CARD_CHARGE);

    private final RecordDebtEventUseCase recordDebtEventUseCase;
    private final JsonMapper jsonMapper;

    /**
     * Procesa un evento del tópico de transacciones.
     *
     * @param message evento en JSON
     */
    @KafkaListener(topics = "${messaging.topics.transaction-events}")
    public void onTransactionEvent(String message) {
        JsonNode event = jsonMapper.readTree(message);
        String eventType = event.path("eventType").asString();
        DebtMovementType type = TYPES.get(eventType);
        if (type == null) {
            log.debug("Ignoring transaction event type={}", eventType);
            return;
        }
        String productId = type == DebtMovementType.CREDIT_PAYMENT
                ? event.get("creditId").asString()
                : event.get("creditCardId").asString();
        DebtMovement movement = new DebtMovement(
                event.get("eventId").asString(),
                event.get("customerId").asString(),
                productId,
                type,
                event.get("amount").decimalValue(),
                LocalDate.parse(event.get("date").asString()));
        recordDebtEventUseCase.recordMovement(movement).blockingAwait();
        log.info("{} recorded customerId={}", eventType, movement.customerId());
    }
}
