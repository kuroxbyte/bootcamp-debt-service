package dev.bootcamp.debtservice.infrastructure.messaging;

import dev.bootcamp.debtservice.application.usecase.RecordDebtEventUseCase;
import dev.bootcamp.debtservice.domain.model.Credit;
import dev.bootcamp.debtservice.domain.model.CreditCard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;

/**
 * Consume el tópico {@code credit-events} de credit-service: créditos y tarjetas otorgados o
 * modificados (nueva fecha de vencimiento o día de pago). Las bajas se ignoran: solo se da de baja
 * un producto sin deuda. El
 * contrato está en {@code docs/events/asyncapi.yaml} del repositorio raíz.
 *
 * <p>Ignora los tipos de evento que no conoce. Si registrar falla, la excepción hace que Kafka
 * vuelva a entregar el mensaje.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreditEventListener {

    private final RecordDebtEventUseCase recordDebtEventUseCase;
    private final JsonMapper jsonMapper;

    /**
     * Procesa un evento del tópico de productos de crédito.
     *
     * @param message evento en JSON
     */
    @KafkaListener(topics = "${messaging.topics.credit-events}")
    public void onCreditEvent(String message) {
        JsonNode event = jsonMapper.readTree(message);
        String eventType = event.path("eventType").asString();
        String customerId = event.path("customerId").asString();
        switch (eventType) {
            // Una modificación trae el estado completo: reemplaza la copia y recalcula la deuda.
            case "CreditGranted", "CreditUpdated" -> recordDebtEventUseCase.recordCredit(new Credit(
                    event.get("creditId").asString(),
                    customerId,
                    event.get("amount").decimalValue(),
                    LocalDate.parse(event.get("dueDate").asString()))).blockingAwait();
            case "CreditCardGranted", "CreditCardUpdated" ->
                    recordDebtEventUseCase.recordCreditCard(new CreditCard(
                    event.get("creditCardId").asString(),
                    customerId,
                    event.get("paymentDueDay").asInt())).blockingAwait();
            default -> {
                log.debug("Ignoring credit event type={}", eventType);
                return;
            }
        }
        log.info("{} recorded customerId={}", eventType, customerId);
    }
}
