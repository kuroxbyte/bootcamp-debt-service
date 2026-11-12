package dev.bootcamp.debtservice.infrastructure.persistence;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Tarjeta de crédito tal como se guarda en la colección {@code credit_cards}; su id es el de
 * credit-service.
 */
@Getter
@AllArgsConstructor
@Document(collection = "credit_cards")
public class CreditCardDocument {
    @Id private final String id;
    @Indexed private final String customerId;
    private final int paymentDueDay;
}
