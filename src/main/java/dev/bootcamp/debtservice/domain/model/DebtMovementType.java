package dev.bootcamp.debtservice.domain.model;

/**
 * Movimiento que cambia la deuda de un producto de crédito.
 */
public enum DebtMovementType {
    /**
     * Pago a un crédito; reduce su deuda.
     */
    CREDIT_PAYMENT,
    /**
     * Pago a una tarjeta de crédito; reduce su deuda.
     */
    CREDIT_CARD_PAYMENT,
    /**
     * Consumo con una tarjeta de crédito; aumenta su deuda.
     */
    CREDIT_CARD_CHARGE
}
