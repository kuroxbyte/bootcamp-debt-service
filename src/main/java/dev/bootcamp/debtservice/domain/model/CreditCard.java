package dev.bootcamp.debtservice.domain.model;

/**
 * Tarjeta de crédito, conocida por el evento {@code CreditCardGranted}.
 *
 * @param id            id de la tarjeta en credit-service
 * @param customerId    cliente dueño
 * @param paymentDueDay día del mes (1 a 28) hasta el que se pagan los consumos del mes anterior
 */
public record CreditCard(String id, String customerId, int paymentDueDay) {}
