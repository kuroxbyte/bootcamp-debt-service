package dev.bootcamp.debtservice.infrastructure.persistence;

import dev.bootcamp.debtservice.application.port.DebtRepository;
import dev.bootcamp.debtservice.domain.model.Credit;
import dev.bootcamp.debtservice.domain.model.CreditCard;
import dev.bootcamp.debtservice.domain.model.CustomerDebt;
import dev.bootcamp.debtservice.domain.model.DebtMovement;
import dev.bootcamp.debtservice.domain.model.DebtMovementType;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Implementa el puerto {@link DebtRepository} con MongoDB reactivo.
 */
@Component
@RequiredArgsConstructor
public class DebtPersistenceAdapter implements DebtRepository {

    private final CreditMongoRepository creditRepository;
    private final CreditCardMongoRepository creditCardRepository;
    private final DebtMovementMongoRepository movementRepository;

    @Override
    public Completable saveCredit(Credit credit) {
        return Completable.fromPublisher(creditRepository.save(new CreditDocument(
                credit.id(),
                credit.customerId(),
                credit.amount(),
                credit.dueDate())));
    }

    @Override
    public Completable saveCreditCard(CreditCard creditCard) {
        return Completable.fromPublisher(creditCardRepository.save(new CreditCardDocument(
                creditCard.id(),
                creditCard.customerId(),
                creditCard.paymentDueDay())));
    }

    @Override
    public Completable saveMovement(DebtMovement movement) {
        return Completable.fromPublisher(movementRepository.save(new DebtMovementDocument(
                movement.eventId(),
                movement.customerId(),
                movement.productId(),
                movement.type().name(),
                movement.amount(),
                movement.date())));
    }

    @Override
    public Single<CustomerDebt> findByCustomerId(String customerId) {
        Mono<CustomerDebt> debt = Mono.zip(
                creditRepository.findByCustomerId(customerId)
                        .map(document -> new Credit(
                                document.getId(),
                                document.getCustomerId(),
                                document.getAmount(),
                                document.getDueDate()))
                        .collectList(),
                creditCardRepository.findByCustomerId(customerId)
                        .map(document -> new CreditCard(
                                document.getId(),
                                document.getCustomerId(),
                                document.getPaymentDueDay()))
                        .collectList(),
                movementRepository.findByCustomerId(customerId)
                        .map(document -> new DebtMovement(
                                document.getId(),
                                document.getCustomerId(),
                                document.getProductId(),
                                DebtMovementType.valueOf(document.getType()),
                                document.getAmount(),
                                document.getDate()))
                        .collectList())
                .map(lists -> new CustomerDebt(
                        customerId,
                        lists.getT1(),
                        lists.getT2(),
                        lists.getT3()));
        return Single.fromPublisher(debt);
    }

    @Override
    public Flowable<String> findCustomerIds() {
        return Flowable.fromPublisher(Flux.merge(
                        creditRepository.findAll().map(CreditDocument::getCustomerId),
                        creditCardRepository.findAll().map(CreditCardDocument::getCustomerId))
                .distinct());
    }
}
