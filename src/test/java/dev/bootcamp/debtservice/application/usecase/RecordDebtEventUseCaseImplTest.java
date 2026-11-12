package dev.bootcamp.debtservice.application.usecase;

import dev.bootcamp.debtservice.application.port.DebtRepository;
import dev.bootcamp.debtservice.domain.model.Credit;
import dev.bootcamp.debtservice.domain.model.CreditCard;
import dev.bootcamp.debtservice.domain.model.DebtMovement;
import dev.bootcamp.debtservice.domain.model.DebtMovementType;
import io.reactivex.rxjava3.core.Completable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecordDebtEventUseCaseImplTest {

    @Mock private DebtRepository debtRepository;

    @Mock private EvaluateDebtUseCase evaluateDebtUseCase;

    @InjectMocks private RecordDebtEventUseCaseImpl useCase;

    @Test
    void savesTheCreditAndThenEvaluatesItsCustomer() {
        Credit credit = new Credit("k1", "c1", BigDecimal.TEN, LocalDate.of(2027, 1, 15));
        when(debtRepository.saveCredit(credit)).thenReturn(Completable.complete());
        when(evaluateDebtUseCase.evaluate("c1")).thenReturn(Completable.complete());

        useCase.recordCredit(credit).test().assertComplete();

        InOrder order = inOrder(debtRepository, evaluateDebtUseCase);
        order.verify(debtRepository).saveCredit(credit);
        order.verify(evaluateDebtUseCase).evaluate("c1");
    }

    @Test
    void savesTheCreditCardAndEvaluatesItsCustomer() {
        CreditCard card = new CreditCard("t1", "c1", 15);
        when(debtRepository.saveCreditCard(card)).thenReturn(Completable.complete());
        when(evaluateDebtUseCase.evaluate("c1")).thenReturn(Completable.complete());

        useCase.recordCreditCard(card).test().assertComplete();

        verify(evaluateDebtUseCase).evaluate("c1");
    }

    @Test
    void doesNotEvaluateIfTheMovementCannotBeSaved() {
        DebtMovement movement = new DebtMovement(
                "e1",
                "c1",
                "t1",
                DebtMovementType.CREDIT_CARD_CHARGE,
                BigDecimal.TEN,
                LocalDate.of(2026, 11, 15));
        when(debtRepository.saveMovement(movement))
                .thenReturn(Completable.error(new IllegalStateException("mongo down")));

        useCase.recordMovement(movement).test().assertError(IllegalStateException.class);

        verify(evaluateDebtUseCase, never()).evaluate("c1");
    }
}
