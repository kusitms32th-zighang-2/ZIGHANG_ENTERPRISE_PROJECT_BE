package zighang2.zighang.web.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class EmbeddingService {

    @Value("${embedding.api-key}")
    private String apiKey;

    @Value("${embedding.url}")
    private String llmUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public float[] getEmbedding(List<String> welfareList) {
        try {
            String text = String.join(" ", welfareList); // 여러 복지를 하나의 문장으로 합침

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", apiKey);

            Map<String, Object> body = Map.of("text", text);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(llmUrl, request, Map.class);

            List<Double> embedding = (List<Double>) ((Map<String, Object>) response.getBody().get("result")).get("embedding");
            float[] vector = new float[embedding.size()];
            for (int i = 0; i < embedding.size(); i++) {
                vector[i] = embedding.get(i).floatValue();
            }
            return vector;

        } catch (Exception e) {
            e.printStackTrace();
            return new float[0];
        }
    }
}

