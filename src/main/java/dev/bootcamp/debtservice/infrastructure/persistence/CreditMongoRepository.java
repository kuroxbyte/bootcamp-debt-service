package dev.bootcamp.debtservice.infrastructure.persistence;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

/**
 * Repositorio de Spring Data para {@link CreditDocument}.
 */
public interface CreditMongoRepository
        extends ReactiveMongoRepository<CreditDocument, String> {

    /**
     * Documentos del cliente.
     */
    Flux<CreditDocument> findByCustomerId(String customerId);
}
