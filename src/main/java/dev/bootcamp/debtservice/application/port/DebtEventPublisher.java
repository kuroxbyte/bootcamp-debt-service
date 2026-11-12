package dev.bootcamp.debtservice.application.port;

import dev.bootcamp.debtservice.domain.model.CustomerDebtStatus;
import io.reactivex.rxjava3.core.Completable;

/**
 * Publicación de los cambios en el estado de deuda de los clientes.
 */
public interface DebtEventPublisher {

    /**
     * Publica que el cliente pasó a tener deuda vencida o dejó de tenerla.
     *
     * @return termina cuando se publicó; error si no se pudo, para volver a intentarlo
     */
    Completable publishStatusChanged(CustomerDebtStatus status);
}
