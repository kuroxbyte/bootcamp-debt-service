package dev.bootcamp.debtservice.infrastructure.persistence;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;

/**
 * Último estado de deuda publicado de un cliente, en la colección {@code debt_status}; su id es
 * el del cliente.
 */
@Getter
@AllArgsConstructor
@Document(collection = "debt_status")
public class DebtStatusDocument {
    @Id private final String customerId;
    private final boolean hasOverdueDebt;

    @Field(targetType = FieldType.DECIMAL128)
    private final BigDecimal overdueAmount;
}
