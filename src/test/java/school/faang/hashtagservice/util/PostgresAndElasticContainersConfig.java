package school.faang.hashtagservice.util;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Objects;

@ActiveProfiles("test")
@Testcontainers
public abstract class PostgresAndElasticContainersConfig {

    @Container
    public static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER =
            new PostgreSQLContainer<>("postgres:13.6");

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

    @DynamicPropertySource
    static void containersProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);

        registry.add("spring.elasticsearch.port", () ->
                ELASTICSEARCH_CONTAINER.getMappedPort(9200));
        registry.add("spring.elasticsearch.host", ELASTICSEARCH_CONTAINER::getHost);
        registry.add("spring.elasticsearch.protocol", () -> "http");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
