package zighang2.zighang.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {
    @Value("${tmap.base-url}")
    private String tmapBaseUrl;

    @Value("${tmap.api-key}")
    private String apiKey;

    @Bean
    public WebClient tmapWebClient() {
        HttpClient httpClient = HttpClient.create();
        return WebClient.builder()
                .baseUrl(tmapBaseUrl)
                .defaultHeader("apiKey", apiKey)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
