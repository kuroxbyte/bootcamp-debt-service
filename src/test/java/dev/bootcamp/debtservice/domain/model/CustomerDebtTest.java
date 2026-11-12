package dev.bootcamp.debtservice.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerDebtTest {

    private static final Credit CREDIT =
            new Credit("k1", "c1", new BigDecimal("1000"), LocalDate.of(2026, 11, 30));
    private static final CreditCard CARD = new CreditCard("t1", "c1", 15);

    private static DebtMovement movement(DebtMovementType type, String product, String amount,
            LocalDate date) {
        return new DebtMovement("e-" + amount + date, "c1", product, type, new BigDecimal(amount),
                date);
    }

    private static CustomerDebt debt(List<Credit> credits, List<CreditCard> cards,
            DebtMovement... movements) {
        return new CustomerDebt("c1", credits, cards, List.of(movements));
    }

    @Test
    void creditIsNotOverdueUntilTheDayAfterItsDueDate() {
        CustomerDebt debt = debt(List.of(CREDIT), List.of());

        assertThat(debt.evaluate(LocalDate.of(2026, 11, 30)).hasOverdueDebt()).isFalse();
        assertThat(debt.evaluate(LocalDate.of(2026, 12, 1)).overdueAmount())
                .isEqualByComparingTo("1000");
    }

    @Test
    void creditPaymentsReduceOrClearTheOverdueAmount() {
        LocalDate day = LocalDate.of(2026, 12, 5);
        CustomerDebt partlyPaid = debt(List.of(CREDIT), List.of(),
                movement(DebtMovementType.CREDIT_PAYMENT, "k1", "400", LocalDate.of(2026, 11, 20)));
        CustomerDebt paid = debt(List.of(CREDIT), List.of(),
                movement(DebtMovementType.CREDIT_PAYMENT, "k1", "400", LocalDate.of(2026, 11, 20)),
                movement(DebtMovementType.CREDIT_PAYMENT, "k1", "600", LocalDate.of(2026, 12, 3)));

        assertThat(partlyPaid.evaluate(day).overdueAmount()).isEqualByComparingTo("600");
        assertThat(paid.evaluate(day).hasOverdueDebt()).isFalse();
    }

    @Test
    void cardChargesOfMonthBecomeOverdueAfterPaymentDayOfNextMonth() {
        CustomerDebt debt = debt(List.of(), List.of(CARD),
                movement(
                        DebtMovementType.CREDIT_CARD_CHARGE,
                        "t1",
                        "300",
                        LocalDate.of(2026, 11, 10)),
                movement(
                        DebtMovementType.CREDIT_CARD_CHARGE,
                        "t1",
                        "50",
                        LocalDate.of(2026, 12, 2)));

        // Los consumos de noviembre se pagan hasta el 15 de diciembre.
        assertThat(debt.evaluate(LocalDate.of(2026, 11, 20)).hasOverdueDebt()).isFalse();
        assertThat(debt.evaluate(LocalDate.of(2026, 12, 15)).hasOverdueDebt()).isFalse();
        // El 16 vence solo lo de noviembre; lo de diciembre vence el 16 de enero.
        assertThat(debt.evaluate(LocalDate.of(2026, 12, 16)).overdueAmount())
                .isEqualByComparingTo("300");
        assertThat(debt.evaluate(LocalDate.of(2027, 1, 16)).overdueAmount())
                .isEqualByComparingTo("350");
    }

    @Test
    void cardPaymentsCoverTheClosedMonths() {
        CustomerDebt debt = debt(List.of(), List.of(CARD),
                movement(
                        DebtMovementType.CREDIT_CARD_CHARGE,
                        "t1",
                        "300",
                        LocalDate.of(2026, 11, 10)),
                movement(
                        DebtMovementType.CREDIT_CARD_PAYMENT,
                        "t1",
                        "300",
                        LocalDate.of(2026, 12, 14)),
                movement(
                        DebtMovementType.CREDIT_CARD_CHARGE,
                        "t1",
                        "80",
                        LocalDate.of(2026, 12, 20)));

        assertThat(debt.evaluate(LocalDate.of(2026, 12, 16)).hasOverdueDebt()).isFalse();
        assertThat(debt.evaluate(LocalDate.of(2027, 1, 16)).overdueAmount())
                .isEqualByComparingTo("80");
    }

    @Test
    void addsTheOverdueAmountOfEveryProduct() {
        CustomerDebt debt = debt(List.of(CREDIT), List.of(CARD),
                movement(
                        DebtMovementType.CREDIT_CARD_CHARGE,
                        "t1",
                        "300",
                        LocalDate.of(2026, 11, 10)),
                movement(
                        DebtMovementType.CREDIT_PAYMENT,
                        "otro",
                        "1000",
                        LocalDate.of(2026, 11, 10)));

        assertThat(debt.evaluate(LocalDate.of(2026, 12, 16)).overdueAmount())
                .isEqualByComparingTo("1300");
    }
}
