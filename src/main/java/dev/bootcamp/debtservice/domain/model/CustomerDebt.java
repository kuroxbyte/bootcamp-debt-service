package dev.bootcamp.debtservice.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;

/**
 * Productos de crédito de un cliente con sus pagos y consumos, para calcular su deuda vencida.
 *
 * @param customerId  id del cliente
 * @param credits     créditos del cliente
 * @param creditCards tarjetas de crédito del cliente
 * @param movements   pagos y consumos de esos productos
 */
public record CustomerDebt(
        String customerId,
        List<Credit> credits,
        List<CreditCard> creditCards,
        List<DebtMovement> movements) {

    /**
     * Calcula el estado de deuda del cliente en una fecha.
     *
     * <ul>
     *   <li>Crédito: desde el día siguiente a su fecha de vencimiento, vence lo que falte pagar
     *       del monto otorgado.
     *   <li>Tarjeta: los consumos de un mes se pagan hasta el día de pago del mes siguiente.
     *       Pasado ese día, vence la parte de los consumos de los meses ya cerrados que los pagos
     *       a la tarjeta no cubren.
     * </ul>
     *
     * @param today fecha de la evaluación
     * @return el monto vencido total (0 si no tiene deuda vencida)
     */
    public CustomerDebtStatus evaluate(LocalDate today) {
        BigDecimal overdue = BigDecimal.ZERO;
        for (Credit credit : credits) {
            overdue = overdue.add(overdueOf(credit, today));
        }
        for (CreditCard creditCard : creditCards) {
            overdue = overdue.add(overdueOf(creditCard, today));
        }
        return new CustomerDebtStatus(customerId, overdue);
    }

    private BigDecimal overdueOf(Credit credit, LocalDate today) {
        if (!today.isAfter(credit.dueDate())) {
            return BigDecimal.ZERO;
        }
        BigDecimal paid = total(movement -> movement.type() == DebtMovementType.CREDIT_PAYMENT
                && movement.productId().equals(credit.id()));
        return positive(credit.amount().subtract(paid));
    }

    private BigDecimal overdueOf(CreditCard creditCard, LocalDate today) {
        // Primer día del mes siguiente al último mes cuyo pago ya venció: los consumos anteriores
        // a esta fecha debían estar pagados.
        LocalDate cutoff = today.getDayOfMonth() > creditCard.paymentDueDay()
                ? today.withDayOfMonth(1)
                : today.withDayOfMonth(1).minusMonths(1);
        BigDecimal dueCharges = total(movement -> movement.type()
                == DebtMovementType.CREDIT_CARD_CHARGE
                && movement.productId().equals(creditCard.id())
                && movement.date().isBefore(cutoff));
        BigDecimal paid = total(movement -> movement.type() == DebtMovementType.CREDIT_CARD_PAYMENT
                && movement.productId().equals(creditCard.id()));
        return positive(dueCharges.subtract(paid));
    }

    private BigDecimal total(Predicate<DebtMovement> filter) {
        return movements.stream()
                .filter(filter)
                .map(DebtMovement::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal positive(BigDecimal amount) {
        return amount.signum() > 0 ? amount : BigDecimal.ZERO;
    }
}
