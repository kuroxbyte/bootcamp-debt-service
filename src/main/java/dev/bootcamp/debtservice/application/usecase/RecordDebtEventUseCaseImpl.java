package dev.bootcamp.debtservice.application.usecase;

import dev.bootcamp.debtservice.application.port.DebtRepository;
import dev.bootcamp.debtservice.domain.model.Credit;
import dev.bootcamp.debtservice.domain.model.CreditCard;
import dev.bootcamp.debtservice.domain.model.DebtMovement;
import io.reactivex.rxjava3.core.Completable;
import lombok.RequiredArgsConstructor;

/**
 * Implementación de {@link RecordDebtEventUseCase}.
 */
@RequiredArgsConstructor
public class RecordDebtEventUseCaseImpl implements RecordDebtEventUseCase {
    private final DebtRepository debtRepository;
    private final EvaluateDebtUseCase evaluateDebtUseCase;

    @Override
    public Completable recordCredit(Credit credit) {
        return debtRepository.saveCredit(credit)
                .andThen(Completable.defer(
                        () -> evaluateDebtUseCase.evaluate(credit.customerId())));
    }

    @Override
    public Completable recordCreditCard(CreditCard creditCard) {
        return debtRepository.saveCreditCard(creditCard)
                .andThen(Completable.defer(
                        () -> evaluateDebtUseCase.evaluate(creditCard.customerId())));
    }

    @Override
    public Completable recordMovement(DebtMovement movement) {
        return debtRepository.saveMovement(movement)
                .andThen(Completable.defer(
                        () -> evaluateDebtUseCase.evaluate(movement.customerId())));
    }
}
