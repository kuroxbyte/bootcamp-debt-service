package dev.bootcamp.debtservice.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Pago o consumo de un producto de crédito, conocido por un evento de transaction-service.
 *
 * @param eventId    id del evento; evita contar dos veces un evento repetido
 * @param customerId cliente dueño del producto
 * @param productId  id del crédito o de la tarjeta
 * @param type       pago a crédito, pago a tarjeta o consumo
 * @param amount     monto
 * @param date       fecha del pago o consumo
 */
public record DebtMovement(
        String eventId,
        String customerId,
        String productId,
        DebtMovementType type,
        BigDecimal amount,
        LocalDate date) {}
