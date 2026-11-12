package dev.bootcamp.debtservice.infrastructure.scheduling;

import dev.bootcamp.debtservice.application.usecase.EvaluateDebtUseCase;
import io.reactivex.rxjava3.core.Completable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DailyDebtEvaluationTest {

    @Mock private EvaluateDebtUseCase evaluateDebtUseCase;

    @InjectMocks private DailyDebtEvaluation dailyDebtEvaluation;

    @Test
    void evaluatesAllCustomers() {
        when(evaluateDebtUseCase.evaluateAll()).thenReturn(Completable.complete());

        dailyDebtEvaluation.evaluateAllCustomers();

        verify(evaluateDebtUseCase).evaluateAll();
    }

    @Test
    void errorsAreLoggedWithoutStoppingTheScheduler() {
        when(evaluateDebtUseCase.evaluateAll())
                .thenReturn(Completable.error(new IllegalStateException("mongo down")));

        assertThatCode(dailyDebtEvaluation::evaluateAllCustomers).doesNotThrowAnyException();
    }
}
