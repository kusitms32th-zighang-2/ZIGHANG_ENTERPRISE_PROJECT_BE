package zighang2.zighang.global.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import zighang2.zighang.web.dto.GeocodePoint;
import zighang2.zighang.web.dto.TmapGeocodingResponseDto;
import zighang2.zighang.web.dto.TmapResponseDto;

import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class TmapClient {

    private final WebClient tmapWebClient;

    @Value("${tmap.base-url}")
    private String baseUrl;

    @Value("${tmap.api-key}")
    private String apiKey;

    @Value("${tmap.coord-type}")
    private String coordType;

    //지오코딩(주소 -> 좌표)
    public Mono<GeocodePoint> geocodeAddress(String address) {

        return tmapWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("tmap/geo/fullAddrGeo")
                        .queryParam("version", 1)
                        .queryParam("format", "json")
                        .queryParam("fullAddr", address)
                        .queryParam("coordType", coordType)
                        .queryParam("addressFlag", "F00")
                        .queryParam("page", 1)
                        .queryParam("count", 20)
                        .build())
                .retrieve()
                .bodyToMono(TmapGeocodingResponseDto.class)
                .flatMap(dto -> {
                    var coords = dto.getCoordinateInfo().getCoordinate();
                    if (coords != null && !coords.isEmpty()) {
                        var c = coords.get(0);
                        return Mono.just(new GeocodePoint(Double.parseDouble(c.getLat()), Double.parseDouble(c.getLon())));
                    } else {
                        return Mono.error(new RuntimeException("좌표를 찾을 수 없습니다."));
                    }
                });
    }

    //자동차 경로 시간 계산
    public Mono<Integer> getDrivingDurationSeconds(GeocodePoint start, GeocodePoint end){
        Map<String, Object> body = new HashMap<>();
        body.put("startX", start.lonAsString());
        body.put("startY", start.latAsString());
        body.put("endX", end.lonAsString());
        body.put("endY", end.latAsString());
        body.put("reqCoordType",coordType);
        body.put("resCoordType",coordType);
        body.put("format", "json");
        body.put("trafficInfo","Y");

        return tmapWebClient.post()
                .uri("tmap/routes")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(TmapResponseDto.TmapRouteResDto.class)
                .map(this::extractCarDurationSeconds);

    }

    //대중교통 경로 시간 계산
    public Mono<Integer> getTransitDurationSeconds(GeocodePoint start, GeocodePoint end){
        Map<String, Object> body = new HashMap<>();
        body.put("startX", start.lonAsString());
        body.put("startY", start.latAsString());
        body.put("endX", end.lonAsString());
        body.put("endY", end.latAsString());
        body.put("format", "json");

        return tmapWebClient.post()
                .uri("transit/routes/sub")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(TmapResponseDto.TmapTransitResDto.class)
                .map(this::extractTransitDurationSeconds);
    }


    // 자동차: features[].properties.totalTime
    public Integer extractCarDurationSeconds(TmapResponseDto.TmapRouteResDto res) {
        if (res == null || res.getFeatures() == null || res.getFeatures().isEmpty()) {
            throw new IllegalStateException("자동차 경로 응답에 features가 없습니다.");
        }
        // 여러 feature가 올 수 있으니 totalTime이 있는 것들 중 최솟값 선택
        return res.getFeatures().stream()
                .map(TmapResponseDto.TmapRouteResDto.Feature::getProperties)
                .filter(Objects::nonNull)
                .map(TmapResponseDto.TmapRouteResDto.Properties::getTotalTime)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElseThrow(() -> new IllegalStateException("자동차 경로 응답에서 totalTime을 찾지 못했습니다."));
    }

    // 대중교통: plan.itineraries[].totalTime
    public Integer extractTransitDurationSeconds(TmapResponseDto.TmapTransitResDto res) {
        if (res == null || res.getPlan() == null || res.getPlan().getItineraries() == null
                || res.getPlan().getItineraries().isEmpty()) {
            throw new IllegalStateException("대중교통 요약 응답에 itineraries가 없습니다.");
        }
        return res.getPlan().getItineraries().stream()
                .map(TmapResponseDto.TmapTransitResDto.Itinerary::getTotalTime)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElseThrow(() -> new IllegalStateException("대중교통 요약 응답에서 totalTime을 찾지 못했습니다."));
    }


}
