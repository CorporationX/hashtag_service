package school.faang.hashtagservice.config.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.JsonpMapper;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import school.faang.hashtagservice.config.properties.ElasticsearchProperties;

@Configuration
@RequiredArgsConstructor
public class ElasticSearchConfig {

    private final ObjectMapper objectMapper;
    private final ElasticsearchProperties config;

    @Bean
    public JsonpMapper jsonpMapper(ObjectMapper objectMapper) {
        return new JacksonJsonpMapper(objectMapper);
    }

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        String host = System.getenv().getOrDefault("ELASTICSEARCH_HOST", config.getHost());
        JsonpMapper mapper = new JacksonJsonpMapper(objectMapper);
        RestClient restClient = RestClient.builder(new HttpHost(host, config.getPort(), config.getProtocol()))
                .build();
        ElasticsearchTransport transport = new RestClientTransport(restClient, mapper);

        return new ElasticsearchClient(transport);
    }
}
