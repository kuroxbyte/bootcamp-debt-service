package dev.bootcamp.debtservice.infrastructure.persistence;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

/**
 * Repositorio de Spring Data para {@link DebtStatusDocument}.
 */
public interface DebtStatusMongoRepository
        extends ReactiveMongoRepository<DebtStatusDocument, String> {}
