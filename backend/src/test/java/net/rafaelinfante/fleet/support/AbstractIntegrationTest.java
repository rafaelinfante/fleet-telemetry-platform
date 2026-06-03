package net.rafaelinfante.fleet.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;

/**
 * Shares one MySQL, RabbitMQ and Mosquitto across the whole integration-test suite (started once
 * at class load). MySQL and RabbitMQ could use {@code @ServiceConnection}, but wiring all three the
 * same way through {@code @DynamicPropertySource} keeps the singleton lifecycle uniform — there is
 * no service-connection support for MQTT.
 */
@SpringBootTest
public abstract class AbstractIntegrationTest {

    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4");

    static final RabbitMQContainer RABBIT = new RabbitMQContainer("rabbitmq:4.3-management");

    static final GenericContainer<?> MOSQUITTO = new GenericContainer<>("eclipse-mosquitto:2.0.22")
            .withExposedPorts(1883)
            .withCopyToContainer(
                    MountableFile.forClasspathResource("mosquitto/mosquitto.conf"),
                    "/mosquitto/config/mosquitto.conf")
            .waitingFor(Wait.forListeningPort());

    static {
        MYSQL.start();
        RABBIT.start();
        MOSQUITTO.start();
    }

    protected static String mqttBrokerUrl() {
        return "tcp://%s:%d".formatted(MOSQUITTO.getHost(), MOSQUITTO.getMappedPort(1883));
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.rabbitmq.host", RABBIT::getHost);
        registry.add("spring.rabbitmq.port", RABBIT::getAmqpPort);
        registry.add("spring.rabbitmq.username", RABBIT::getAdminUsername);
        registry.add("spring.rabbitmq.password", RABBIT::getAdminPassword);
        registry.add("fleet.mqtt.url", AbstractIntegrationTest::mqttBrokerUrl);
    }
}
