package dev.bootcamp.debtservice.application.port;

import dev.bootcamp.debtservice.domain.model.CustomerDebtStatus;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;

/**
 * Último estado de deuda publicado de cada cliente, para publicar solo los cambios.
 */
public interface DebtStatusRepository {

    /**
     * Si el último estado publicado del cliente tenía deuda vencida.
     *
     * @return true o false, o vacío si nunca se publicó un estado para él
     */
    Maybe<Boolean> findLastHasOverdueDebt(String customerId);

    /**
     * Guarda el estado como el último publicado.
     */
    Completable save(CustomerDebtStatus status);
}
