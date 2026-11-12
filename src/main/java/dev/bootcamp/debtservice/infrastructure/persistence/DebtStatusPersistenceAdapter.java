package dev.bootcamp.debtservice.infrastructure.persistence;

import dev.bootcamp.debtservice.application.port.DebtStatusRepository;
import dev.bootcamp.debtservice.domain.model.CustomerDebtStatus;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Implementa el puerto {@link DebtStatusRepository} con MongoDB reactivo.
 */
@Component
@RequiredArgsConstructor
public class DebtStatusPersistenceAdapter implements DebtStatusRepository {

    private final DebtStatusMongoRepository mongoRepository;

    @Override
    public Maybe<Boolean> findLastHasOverdueDebt(String customerId) {
        return Maybe.fromPublisher(mongoRepository.findById(customerId))
                .map(DebtStatusDocument::isHasOverdueDebt);
    }

    @Override
    public Completable save(CustomerDebtStatus status) {
        return Completable.fromPublisher(mongoRepository.save(new DebtStatusDocument(
                status.customerId(),
                status.hasOverdueDebt(),
                status.overdueAmount())));
    }
}
