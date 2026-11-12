package dev.bootcamp.debtservice.infrastructure.persistence;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

/**
 * Repositorio de Spring Data para {@link CreditCardDocument}.
 */
public interface CreditCardMongoRepository
        extends ReactiveMongoRepository<CreditCardDocument, String> {

    /**
     * Documentos del cliente.
     */
    Flux<CreditCardDocument> findByCustomerId(String customerId);
}
