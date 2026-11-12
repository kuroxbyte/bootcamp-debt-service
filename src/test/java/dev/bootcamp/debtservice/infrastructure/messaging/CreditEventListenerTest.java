package dev.bootcamp.debtservice.infrastructure.messaging;

import dev.bootcamp.debtservice.application.usecase.RecordDebtEventUseCase;
import dev.bootcamp.debtservice.domain.model.Credit;
import dev.bootcamp.debtservice.domain.model.CreditCard;
import io.reactivex.rxjava3.core.Completable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreditEventListenerTest {

    @Mock private RecordDebtEventUseCase recordDebtEventUseCase;

    private CreditEventListener listener;

    @BeforeEach
    void createListener() {
        listener = new CreditEventListener(recordDebtEventUseCase, JsonMapper.builder().build());
    }

    @Test
    void recordsCreditGranted() {
        Credit credit = new Credit("k1", "c1", new BigDecimal("5000"), LocalDate.of(2027, 1, 15));
        when(recordDebtEventUseCase.recordCredit(credit)).thenReturn(Completable.complete());

        listener.onCreditEvent("{\"eventType\":\"CreditGranted\",\"eventId\":\"e1\","
                + "\"creditId\":\"k1\",\"customerId\":\"c1\",\"creditType\":\"PERSONAL\","
                + "\"amount\":5000,\"dueDate\":\"2027-01-15\",\"grantedAt\":\"2026-11-15\"}");

        verify(recordDebtEventUseCase).recordCredit(credit);
    }

    @Test
    void recordsCreditCardGranted() {
        CreditCard card = new CreditCard("t1", "c1", 15);
        when(recordDebtEventUseCase.recordCreditCard(card)).thenReturn(Completable.complete());

        listener.onCreditEvent("{\"eventType\":\"CreditCardGranted\",\"eventId\":\"e2\","
                + "\"creditCardId\":\"t1\",\"customerId\":\"c1\",\"creditLimit\":1000,"
                + "\"paymentDueDay\":15,\"grantedAt\":\"2026-11-15\"}");

        verify(recordDebtEventUseCase).recordCreditCard(card);
    }

    @Test
    void ignoresUnknownEventTypes() {
        listener.onCreditEvent("{\"eventType\":\"Other\",\"customerId\":\"c1\"}");

        verifyNoInteractions(recordDebtEventUseCase);
    }
}
