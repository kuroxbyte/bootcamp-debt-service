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
 * Crédito tal como se guarda en la colección {@code credits}; su id es el de credit-service.
 */
@Getter
@AllArgsConstructor
@Document(collection = "credits")
public class CreditDocument {
    @Id private final String id;
    @Indexed private final String customerId;

    @Field(targetType = FieldType.DECIMAL128)
    private final BigDecimal amount;

    private final LocalDate dueDate;
}
