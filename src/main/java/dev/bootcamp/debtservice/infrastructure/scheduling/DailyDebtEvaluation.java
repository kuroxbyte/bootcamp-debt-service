package dev.bootcamp.debtservice.infrastructure.scheduling;

import dev.bootcamp.debtservice.application.usecase.EvaluateDebtUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Revisión diaria de la deuda de todos los clientes: una deuda vence al pasar su fecha, sin que
 * llegue ningún evento. El horario se configura con {@code debt.evaluation.cron}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DailyDebtEvaluation {

    private final EvaluateDebtUseCase evaluateDebtUseCase;

    /**
     * Evalúa la deuda de todos los clientes con productos de crédito.
     */
    @Scheduled(cron = "${debt.evaluation.cron}")
    public void evaluateAllCustomers() {
        log.info("Daily debt evaluation started");
        evaluateDebtUseCase.evaluateAll()
                .doOnComplete(() -> log.info("Daily debt evaluation finished"))
                .onErrorComplete(error -> {
                    log.error("Daily debt evaluation finished with errors", error);
                    return true;
                })
                .blockingAwait();
    }
}
