package dev.bootcamp.debtservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Microservicio de deudas: calcula si cada cliente tiene deuda vencida en algún producto de
 * crédito, solo con eventos de Kafka.
 */
@SpringBootApplication
public class DebtServiceApplication {

    /**
     * Arranca el servicio.
     *
     * @param args argumentos de la línea de comandos
     */
    public static void main(String[] args) {
        SpringApplication.run(DebtServiceApplication.class, args);
    }
}
