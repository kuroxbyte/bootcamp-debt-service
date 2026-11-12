package dev.bootcamp.debtservice.application.port;

import dev.bootcamp.debtservice.domain.model.Credit;
import dev.bootcamp.debtservice.domain.model.CreditCard;
import dev.bootcamp.debtservice.domain.model.CustomerDebt;
import dev.bootcamp.debtservice.domain.model.DebtMovement;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * Productos de crédito, pagos y consumos conocidos por eventos. Guardar dos veces el mismo
 * producto o el mismo evento no duplica nada.
 */
public interface DebtRepository {

    /**
     * Guarda o reemplaza el crédito.
     */
    Completable saveCredit(Credit credit);

    /**
     * Guarda o reemplaza la tarjeta de crédito.
     */
    Completable saveCreditCard(CreditCard creditCard);

    /**
     * Guarda el pago o consumo; si ya existe uno con el mismo id de evento, no hace nada.
     */
    Completable saveMovement(DebtMovement movement);

    /**
     * Productos de crédito del cliente con sus pagos y consumos (listas vacías si no tiene).
     */
    Single<CustomerDebt> findByCustomerId(String customerId);

    /**
     * Ids de los clientes que tienen algún producto de crédito, sin repetir.
     */
    Flowable<String> findCustomerIds();
}
