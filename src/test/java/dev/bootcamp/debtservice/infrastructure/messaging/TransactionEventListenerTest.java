package dev.bootcamp.debtservice.infrastructure.messaging;

import dev.bootcamp.debtservice.application.usecase.RecordDebtEventUseCase;
import dev.bootcamp.debtservice.domain.model.DebtMovement;
import dev.bootcamp.debtservice.domain.model.DebtMovementType;
import io.reactivex.rxjava3.core.Completable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionEventListenerTest {

    @Mock private RecordDebtEventUseCase recordDebtEventUseCase;

    private TransactionEventListener listener;

    @BeforeEach
    void createListener() {
        listener = new TransactionEventListener(
                recordDebtEventUseCase,
                JsonMapper.builder().build());
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
        "CreditPaymentRegistered, creditId, CREDIT_PAYMENT",
        "CreditCardPaymentRegistered, creditCardId, CREDIT_CARD_PAYMENT",
        "CreditCardChargeRegistered, creditCardId, CREDIT_CARD_CHARGE"
    })
    void recordsEachMovementType(String eventType, String productField, DebtMovementType type) {
        DebtMovement movement = new DebtMovement(
                "e1",
                "c1",
                "p1",
                type,
                new BigDecimal("75.5"),
                LocalDate.of(2026, 11, 15));
        when(recordDebtEventUseCase.recordMovement(movement)).thenReturn(Completable.complete());

        listener.onTransactionEvent("{\"eventType\":\"" + eventType + "\",\"eventId\":\"e1\","
                + "\"" + productField + "\":\"p1\",\"customerId\":\"c1\",\"amount\":75.5,"
                + "\"date\":\"2026-11-15\",\"occurredAt\":\"2026-11-15T10:00:00Z\"}");

        verify(recordDebtEventUseCase).recordMovement(movement);
    }

    @Test
    void ignoresUnknownEventTypes() {
        listener.onTransactionEvent("{\"eventType\":\"DepositRegistered\",\"customerId\":\"c1\"}");

        verifyNoInteractions(recordDebtEventUseCase);
    }
}
