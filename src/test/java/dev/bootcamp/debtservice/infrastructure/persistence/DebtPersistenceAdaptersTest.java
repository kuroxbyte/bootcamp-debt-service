package dev.bootcamp.debtservice.infrastructure.persistence;

import dev.bootcamp.debtservice.domain.model.Credit;
import dev.bootcamp.debtservice.domain.model.CreditCard;
import dev.bootcamp.debtservice.domain.model.CustomerDebt;
import dev.bootcamp.debtservice.domain.model.CustomerDebtStatus;
import dev.bootcamp.debtservice.domain.model.DebtMovement;
import dev.bootcamp.debtservice.domain.model.DebtMovementType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataMongoTest
@Import({DebtPersistenceAdapter.class, DebtStatusPersistenceAdapter.class})
class DebtPersistenceAdaptersTest {

    @Container @ServiceConnection static MongoDBContainer mongo = new MongoDBContainer("mongo:7.0");

    private static final LocalDate DATE = LocalDate.of(2026, 11, 15);

    @Autowired private DebtPersistenceAdapter debtAdapter;

    @Autowired private DebtStatusPersistenceAdapter statusAdapter;

    @Autowired private ReactiveMongoTemplate mongoTemplate;

    @BeforeEach
    void cleanDatabase() {
        mongoTemplate.getMongoDatabase().flatMap(db -> Mono.from(db.drop())).block();
    }

    private static DebtMovement charge(String eventId, String customerId) {
        return new DebtMovement(
                eventId,
                customerId,
                "t1",
                DebtMovementType.CREDIT_CARD_CHARGE,
                new BigDecimal("80.50"),
                DATE);
    }

    @Test
    void loadsTheCustomerProductsWithTheirMovements() {
        Credit credit = new Credit("k1", "c1", new BigDecimal("1000"), LocalDate.of(2027, 1, 15));
        CreditCard card = new CreditCard("t1", "c1", 15);
        debtAdapter.saveCredit(credit).blockingAwait();
        debtAdapter.saveCreditCard(card).blockingAwait();
        debtAdapter.saveMovement(charge("e1", "c1")).blockingAwait();
        debtAdapter.saveCreditCard(new CreditCard("t2", "c2", 10)).blockingAwait();

        CustomerDebt debt = debtAdapter.findByCustomerId("c1").blockingGet();

        assertThat(debt.credits()).hasSize(1);
        assertThat(debt.credits().get(0).amount()).isEqualByComparingTo("1000");
        assertThat(debt.credits().get(0).dueDate()).isEqualTo(LocalDate.of(2027, 1, 15));
        assertThat(debt.creditCards()).containsExactly(card);
        assertThat(debt.movements()).hasSize(1);
        assertThat(debt.movements().get(0).amount()).isEqualByComparingTo("80.50");
    }

    @Test
    void repeatedEventIsSavedOnlyOnce() {
        debtAdapter.saveMovement(charge("e1", "c1")).blockingAwait();
        debtAdapter.saveMovement(charge("e1", "c1")).blockingAwait();

        assertThat(debtAdapter.findByCustomerId("c1").blockingGet().movements()).hasSize(1);
    }

    @Test
    void listsCustomersWithCreditProductsOnce() {
        debtAdapter.saveCredit(new Credit("k1", "c1", BigDecimal.TEN, DATE)).blockingAwait();
        debtAdapter.saveCreditCard(new CreditCard("t1", "c1", 15)).blockingAwait();
        debtAdapter.saveCreditCard(new CreditCard("t2", "c2", 15)).blockingAwait();

        assertThat(debtAdapter.findCustomerIds().toList().blockingGet())
                .containsExactlyInAnyOrder("c1", "c2");
    }

    @Test
    void keepsTheLastPublishedStatus() {
        statusAdapter.findLastHasOverdueDebt("c1").test().awaitDone(5, TimeUnit.SECONDS)
                .assertNoValues();

        statusAdapter.save(new CustomerDebtStatus("c1", BigDecimal.TEN)).blockingAwait();
        statusAdapter.findLastHasOverdueDebt("c1").test().awaitDone(5, TimeUnit.SECONDS)
                .assertValue(true);

        statusAdapter.save(new CustomerDebtStatus("c1", BigDecimal.ZERO)).blockingAwait();
        statusAdapter.findLastHasOverdueDebt("c1").test().awaitDone(5, TimeUnit.SECONDS)
                .assertValue(false);
    }
}
