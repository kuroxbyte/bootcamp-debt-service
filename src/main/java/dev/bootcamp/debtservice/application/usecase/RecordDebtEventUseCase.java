package dev.bootcamp.debtservice.application.usecase;

import dev.bootcamp.debtservice.domain.model.Credit;
import dev.bootcamp.debtservice.domain.model.CreditCard;
import dev.bootcamp.debtservice.domain.model.DebtMovement;
import io.reactivex.rxjava3.core.Completable;

/**
 * Caso de uso para registrar los eventos que cambian la deuda de un cliente: productos de crédito
 * otorgados (credit-service) y pagos y consumos (transaction-service).
 *
 * <p>Después de guardar, evalúa de nuevo la deuda del cliente (ver
 * {@link EvaluateDebtUseCase}). Procesar dos veces el mismo evento no cambia el resultado. Es
 * reactivo: no hace nada hasta que se suscribe al {@link Completable}.
 */
public interface RecordDebtEventUseCase {

    /**
     * Registra un crédito otorgado.
     */
    Completable recordCredit(Credit credit);

    /**
     * Registra una tarjeta de crédito otorgada.
     */
    Completable recordCreditCard(CreditCard creditCard);

    /**
     * Registra un pago o consumo.
     */
    Completable recordMovement(DebtMovement movement);
}
