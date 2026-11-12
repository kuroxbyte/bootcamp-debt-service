package dev.bootcamp.debtservice.application.usecase;

import dev.bootcamp.debtservice.application.port.DebtEventPublisher;
import dev.bootcamp.debtservice.application.port.DebtRepository;
import dev.bootcamp.debtservice.application.port.DebtStatusRepository;
import dev.bootcamp.debtservice.domain.model.CustomerDebtStatus;
import io.reactivex.rxjava3.core.Completable;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Implementación de {@link EvaluateDebtUseCase}.
 */
public class EvaluateDebtUseCaseImpl implements EvaluateDebtUseCase {
    private final DebtRepository debtRepository;
    private final DebtStatusRepository debtStatusRepository;
    private final DebtEventPublisher debtEventPublisher;
    private final Clock clock;

    /**
     * Crea el caso de uso con los adaptadores de sus puertos y el reloj de la evaluación.
     */
    public EvaluateDebtUseCaseImpl(
            DebtRepository debtRepository,
            DebtStatusRepository debtStatusRepository,
            DebtEventPublisher debtEventPublisher,
            Clock clock) {
        this.debtRepository = debtRepository;
        this.debtStatusRepository = debtStatusRepository;
        this.debtEventPublisher = debtEventPublisher;
        this.clock = clock;
    }

    @Override
    public Completable evaluate(String customerId) {
        return debtRepository.findByCustomerId(customerId)
                .map(debt -> debt.evaluate(LocalDate.now(clock)))
                .flatMapCompletable(status -> debtStatusRepository
                        .findLastHasOverdueDebt(customerId)
                        .defaultIfEmpty(false)
                        .flatMapCompletable(lastHasOverdueDebt ->
                                lastHasOverdueDebt == status.hasOverdueDebt()
                                        ? Completable.complete()
                                        : publishAndSave(status)));
    }

    @Override
    public Completable evaluateAll() {
        return debtRepository.findCustomerIds().concatMapCompletableDelayError(this::evaluate);
    }

    private Completable publishAndSave(CustomerDebtStatus status) {
        return debtEventPublisher.publishStatusChanged(status)
                .andThen(Completable.defer(() -> debtStatusRepository.save(status)));
    }
}
