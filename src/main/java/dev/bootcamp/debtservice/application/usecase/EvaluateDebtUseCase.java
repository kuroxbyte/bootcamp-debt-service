package dev.bootcamp.debtservice.application.usecase;

import dev.bootcamp.debtservice.domain.model.CustomerDebt;
import io.reactivex.rxjava3.core.Completable;

/**
 * Caso de uso para evaluar si un cliente tiene deuda vencida y publicar los cambios.
 *
 * <ul>
 *   <li>Calcula el monto vencido con {@link CustomerDebt#evaluate} en la fecha actual.
 *   <li>Publica {@code CustomerDebtStatusChanged} solo si el cliente pasó a tener deuda vencida o
 *       dejó de tenerla respecto del último estado publicado. Un cliente sin estado publicado se
 *       considera sin deuda vencida.
 *   <li>Guarda el estado como publicado solo después de publicarlo; si la publicación falla,
 *       termina con error para volver a intentarlo.
 * </ul>
 *
 * <p>Es reactivo: no hace nada hasta que se suscribe al {@link Completable}.
 */
public interface EvaluateDebtUseCase {

    /**
     * Evalúa la deuda de un cliente.
     */
    Completable evaluate(String customerId);

    /**
     * Evalúa la deuda de todos los clientes con productos de crédito; la revisión diaria la usa
     * para detectar las deudas que vencen sin que llegue ningún evento. Si falla un cliente, sigue
     * con los demás y termina con error al final.
     */
    Completable evaluateAll();
}
