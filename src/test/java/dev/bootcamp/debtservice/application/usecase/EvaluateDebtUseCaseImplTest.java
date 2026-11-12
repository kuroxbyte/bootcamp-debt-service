package dev.bootcamp.debtservice.application.usecase;

import dev.bootcamp.debtservice.application.port.DebtEventPublisher;
import dev.bootcamp.debtservice.application.port.DebtRepository;
import dev.bootcamp.debtservice.application.port.DebtStatusRepository;
import dev.bootcamp.debtservice.domain.model.Credit;
import dev.bootcamp.debtservice.domain.model.CustomerDebt;
import dev.bootcamp.debtservice.domain.model.CustomerDebtStatus;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvaluateDebtUseCaseImplTest {

    /**
     * Hoy es 2026-12-01: el crédito que vence el 2026-11-30 ya está vencido.
     */
    private static final Clock CLOCK =
            Clock.fixed(Instant.parse("2026-12-01T10:00:00Z"), ZoneOffset.UTC);

    private static final CustomerDebt OVERDUE = new CustomerDebt(
            "c1",
            List.of(new Credit("k1", "c1", new BigDecimal("1000"), LocalDate.of(2026, 11, 30))),
            List.of(),
            List.of());
    private static final CustomerDebt NOT_OVERDUE = new CustomerDebt(
            "c1",
            List.of(new Credit("k1", "c1", new BigDecimal("1000"), LocalDate.of(2027, 11, 30))),
            List.of(),
            List.of());

    @Mock private DebtRepository debtRepository;

    @Mock private DebtStatusRepository debtStatusRepository;

    @Mock private DebtEventPublisher debtEventPublisher;

    private EvaluateDebtUseCaseImpl useCase;

    @BeforeEach
    void createUseCase() {
        useCase = new EvaluateDebtUseCaseImpl(
                debtRepository,
                debtStatusRepository,
                debtEventPublisher,
                CLOCK);
    }

    @Test
    void publishesAndSavesWhenTheCustomerBecomesOverdue() {
        CustomerDebtStatus overdue = new CustomerDebtStatus("c1", new BigDecimal("1000"));
        when(debtRepository.findByCustomerId("c1")).thenReturn(Single.just(OVERDUE));
        when(debtStatusRepository.findLastHasOverdueDebt("c1")).thenReturn(Maybe.just(false));
        when(debtEventPublisher.publishStatusChanged(overdue)).thenReturn(Completable.complete());
        when(debtStatusRepository.save(overdue)).thenReturn(Completable.complete());

        useCase.evaluate("c1").test().assertComplete();

        verify(debtStatusRepository).save(overdue);
    }

    @Test
    void publishesWhenTheOverdueDebtIsPaid() {
        CustomerDebtStatus clear = new CustomerDebtStatus("c1", BigDecimal.ZERO);
        when(debtRepository.findByCustomerId("c1")).thenReturn(Single.just(NOT_OVERDUE));
        when(debtStatusRepository.findLastHasOverdueDebt("c1")).thenReturn(Maybe.just(true));
        when(debtEventPublisher.publishStatusChanged(clear)).thenReturn(Completable.complete());
        when(debtStatusRepository.save(clear)).thenReturn(Completable.complete());

        useCase.evaluate("c1").test().assertComplete();

        verify(debtEventPublisher).publishStatusChanged(clear);
    }

    @Test
    void doesNotPublishWhenNothingChanged() {
        when(debtRepository.findByCustomerId("c1")).thenReturn(Single.just(OVERDUE));
        when(debtStatusRepository.findLastHasOverdueDebt("c1")).thenReturn(Maybe.just(true));

        useCase.evaluate("c1").test().assertComplete();

        verify(debtEventPublisher, never()).publishStatusChanged(any());
    }

    @Test
    void customerWithoutPublishedStatusIsConsideredWithoutOverdueDebt() {
        when(debtRepository.findByCustomerId("c1")).thenReturn(Single.just(NOT_OVERDUE));
        when(debtStatusRepository.findLastHasOverdueDebt("c1")).thenReturn(Maybe.empty());

        useCase.evaluate("c1").test().assertComplete();

        verify(debtEventPublisher, never()).publishStatusChanged(any());
    }

    @Test
    void doesNotSaveTheStatusWhenPublishingFails() {
        when(debtRepository.findByCustomerId("c1")).thenReturn(Single.just(OVERDUE));
        when(debtStatusRepository.findLastHasOverdueDebt("c1")).thenReturn(Maybe.empty());
        when(debtEventPublisher.publishStatusChanged(any()))
                .thenReturn(Completable.error(new IllegalStateException("kafka down")));

        useCase.evaluate("c1").test().assertError(IllegalStateException.class);

        verify(debtStatusRepository, never()).save(any());
    }

    @Test
    void evaluatesEveryCustomerAndReportsErrorsAtTheEnd() {
        when(debtRepository.findCustomerIds()).thenReturn(Flowable.just("c1", "c2"));
        when(debtRepository.findByCustomerId("c1"))
                .thenReturn(Single.error(new IllegalStateException("mongo down")));
        when(debtRepository.findByCustomerId("c2")).thenReturn(Single.just(new CustomerDebt(
                "c2",
                List.of(),
                List.of(),
                List.of())));
        when(debtStatusRepository.findLastHasOverdueDebt("c2")).thenReturn(Maybe.empty());

        useCase.evaluateAll().test().assertError(IllegalStateException.class);

        verify(debtStatusRepository).findLastHasOverdueDebt("c2");
    }
}
