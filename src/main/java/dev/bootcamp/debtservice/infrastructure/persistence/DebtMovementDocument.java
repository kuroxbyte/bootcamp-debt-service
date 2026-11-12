package dev.bootcamp.debtservice.infrastructure.persistence;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Pago o consumo tal como se guarda en la colección {@code debt_movements}. Su id es el del
 * evento, así un evento repetido reemplaza al mismo documento en lugar de duplicarlo.
 */
@Getter
@AllArgsConstructor
@Document(collection = "debt_movements")
public class DebtMovementDocument {
    @Id private final String id;
    @Indexed private final String customerId;
    private final String productId;

    /**
     * CREDIT_PAYMENT, CREDIT_CARD_PAYMENT o CREDIT_CARD_CHARGE.
     */
    private final String type;

    @Field(targetType = FieldType.DECIMAL128)
    private final BigDecimal amount;

    private final LocalDate date;
}
