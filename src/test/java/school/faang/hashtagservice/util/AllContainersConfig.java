package school.faang.hashtagservice.util;

import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Objects;

@ActiveProfiles("test")
@Testcontainers
public class AllContainersConfig {

    @Container
    public static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER =
            new PostgreSQLContainer<>("postgres:13.6");

    @Container
    public static final RedisContainer REDIS_CONTAINER =
            new RedisContainer(DockerImageName.parse("redis/redis-stack:latest"));

    @Container
    public static final ElasticsearchContainer ELASTICSEARCH_CONTAINER =
            new ElasticsearchContainer(DockerImageName.parse("elasticsearch:8.6.2")
                    .asCompatibleSubstituteFor("docker.elastic.co/elasticsearch/elasticsearch"))
                    .withEnv("ES_JAVA_OPTS", "-Xms256m -Xmx256m")
                    .withEnv("discovery.type", "single-node")
                    .withEnv("bootstrap.memory_lock", "false")
                    .withEnv("xpack.security.enabled", "false")
                    .withEnv("cluster.routing.allocation.disk.threshold_enabled", "false")
                    .withCreateContainerCmdModifier(cmd ->
                            Objects.requireNonNull(cmd.getHostConfig())
                                    .withMemory(512 * 1024 * 1024L)
                                    .withMemorySwap(0L)
                                    .withCpuCount(1L));

    @Container
    public static final KafkaContainer KAFKA_CONTAINER =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.0.0"));

    @DynamicPropertySource
    static void containersProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);

        registry.add("spring.data.redis.port", () ->
                REDIS_CONTAINER.getMappedPort(6379));
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);

        registry.add("spring.elasticsearch.port", () ->
                ELASTICSEARCH_CONTAINER.getMappedPort(9200));
        registry.add("spring.elasticsearch.host", ELASTICSEARCH_CONTAINER::getHost);
        registry.add("spring.elasticsearch.protocol", () -> "http");

        registry.add("spring.kafka.bootstrap-servers", KAFKA_CONTAINER::getBootstrapServers);
        registry.add("spring.kafka.consumer.group-id", () -> "hashtag-group");
        registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
        registry.add("spring.kafka.producer.acks", () -> "1");

        registry.add("spring.kafka.topics.hashtag-adding.name", () -> "hashtag-adding-topic");
        registry.add("spring.kafka.topics.hashtag-removing.name", () -> "hashtag-removing-topic");
        registry.add("spring.kafka.topics.hashtag-analytics.name", () -> "hashtag-analytics-topic");
        registry.add("spring.kafka.topics.hashtag-notification.name", () -> "hashtag-notification-topic");
        registry.add("spring.kafka.topics.hashtag-achievement.name", () -> "hashtag-achievement-topic");

        registry.add("spring.kafka.topics.hashtag-adding.partitions", () -> 3);
        registry.add("spring.kafka.topics.hashtag-adding.replicas", () -> 1);
        registry.add("spring.kafka.topics.hashtag-removing.partitions", () -> 3);
        registry.add("spring.kafka.topics.hashtag-removing.replicas", () -> 1);
        registry.add("spring.kafka.topics.hashtag-analytics.partitions", () -> 3);
        registry.add("spring.kafka.topics.hashtag-analytics.replicas", () -> 1);
        registry.add("spring.kafka.topics.hashtag-notification.partitions", () -> 3);
        registry.add("spring.kafka.topics.hashtag-notification.replicas", () -> 1);
        registry.add("spring.kafka.topics.hashtag-achievement.partitions", () -> 3);
        registry.add("spring.kafka.topics.hashtag-achievement.replicas", () -> 1);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeAll
    static void setUp() throws Exception {
        createKafkaTopic("hashtag-adding-topic");
        createKafkaTopic("hashtag-removing-topic");
        createKafkaTopic("hashtag-analytics-topic");
        createKafkaTopic("hashtag-notification-topic");
        createKafkaTopic("hashtag-achievement-topic");
    }

    private static void createKafkaTopic(String name) throws Exception {
        KAFKA_CONTAINER.execInContainer(
                "kafka-topics",
                "--create",
                "--bootstrap-server", "PLAINTEXT://localhost:9092",
                "--topic", name,
                "--partitions", String.valueOf(3),
                "--replication-factor", String.valueOf(1)
        );
    }
}
