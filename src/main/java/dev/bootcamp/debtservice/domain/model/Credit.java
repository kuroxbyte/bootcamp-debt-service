package dev.bootcamp.debtservice.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Crédito personal o empresarial, conocido por el evento {@code CreditGranted}.
 *
 * @param id         id del crédito en credit-service
 * @param customerId cliente dueño
 * @param amount     monto otorgado
 * @param dueDate    fecha de vencimiento
 */
public record Credit(String id, String customerId, BigDecimal amount, LocalDate dueDate) {}
