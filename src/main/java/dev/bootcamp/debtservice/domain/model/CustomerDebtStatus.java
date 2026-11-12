package dev.bootcamp.debtservice.domain.model;

import java.math.BigDecimal;

/**
 * Resultado de evaluar la deuda de un cliente.
 *
 * @param customerId    id del cliente
 * @param overdueAmount monto vencido (0 si no tiene deuda vencida)
 */
public record CustomerDebtStatus(String customerId, BigDecimal overdueAmount) {

    /**
     * Indica si el cliente tiene deuda vencida en algún producto de crédito.
     */
    public boolean hasOverdueDebt() {
        return overdueAmount.signum() > 0;
    }
}
