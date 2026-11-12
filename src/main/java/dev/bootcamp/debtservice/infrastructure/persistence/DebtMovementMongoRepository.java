package dev.bootcamp.debtservice.infrastructure.persistence;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

/**
 * Repositorio de Spring Data para {@link DebtMovementDocument}.
 */
public interface DebtMovementMongoRepository
        extends ReactiveMongoRepository<DebtMovementDocument, String> {

    /**
     * Documentos del cliente.
     */
    Flux<DebtMovementDocument> findByCustomerId(String customerId);
}
