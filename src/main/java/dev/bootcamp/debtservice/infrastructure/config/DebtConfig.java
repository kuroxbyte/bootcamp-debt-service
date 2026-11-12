package dev.bootcamp.debtservice.infrastructure.config;

import dev.bootcamp.debtservice.application.port.DebtEventPublisher;
import dev.bootcamp.debtservice.application.port.DebtRepository;
import dev.bootcamp.debtservice.application.port.DebtStatusRepository;
import dev.bootcamp.debtservice.application.usecase.EvaluateDebtUseCase;
import dev.bootcamp.debtservice.application.usecase.EvaluateDebtUseCaseImpl;
import dev.bootcamp.debtservice.application.usecase.RecordDebtEventUseCase;
import dev.bootcamp.debtservice.application.usecase.RecordDebtEventUseCaseImpl;
import dev.bootcamp.debtservice.infrastructure.messaging.DebtKafkaEventPublisher;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.time.Duration;

/**
 * Registra los casos de uso, el publicador de eventos y la revisión diaria.
 */
@Configuration
@EnableScheduling
public class DebtConfig {

    /**
     * Reloj de la evaluación de deudas.
     *
     * <p>{@code debt.evaluation.days-ahead} adelanta la fecha de evaluación; vale 0 salvo en las
     * pruebas de punta a punta ({@code docker-compose.e2e.yml}), que lo usan para vencer deudas
     * sin esperar.
     */
    @Bean
    public Clock evaluationClock(@Value("${debt.evaluation.days-ahead:0}") long daysAhead) {
        return Clock.offset(Clock.systemDefaultZone(), Duration.ofDays(daysAhead));
    }

    /**
     * Registra el caso de uso {@link EvaluateDebtUseCase}.
     */
    @Bean
    public EvaluateDebtUseCase evaluateDebtUseCase(
            DebtRepository debtRepository,
            DebtStatusRepository debtStatusRepository,
            DebtEventPublisher debtEventPublisher,
            Clock evaluationClock) {
        return new EvaluateDebtUseCaseImpl(
                debtRepository,
                debtStatusRepository,
                debtEventPublisher,
                evaluationClock);
    }

    /**
     * Registra el caso de uso {@link RecordDebtEventUseCase}.
     */
    @Bean
    public RecordDebtEventUseCase recordDebtEventUseCase(
            DebtRepository debtRepository,
            EvaluateDebtUseCase evaluateDebtUseCase) {
        return new RecordDebtEventUseCaseImpl(debtRepository, evaluateDebtUseCase);
    }

    /**
     * Publicador de {@code CustomerDebtStatusChanged}; fecha los eventos con el reloj real.
     */
    @Bean
    public DebtEventPublisher debtEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            JsonMapper jsonMapper,
            @Value("${messaging.topics.debt-events}") String topic) {
        return new DebtKafkaEventPublisher(kafkaTemplate, jsonMapper, Clock.systemUTC(), topic);
    }

    /**
     * Tópico de cambios de estado de deuda; debt-service es su dueño. La clave de cada mensaje es
     * el id del cliente.
     */
    @Bean
    public NewTopic debtEventsTopic(@Value("${messaging.topics.debt-events}") String topic) {
        return TopicBuilder.name(topic).partitions(3).replicas(1).build();
    }
}
