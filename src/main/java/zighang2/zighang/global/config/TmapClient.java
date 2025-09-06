package zighang2.zighang.global.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import zighang2.zighang.web.dto.tmap.GeocodePoint;
import zighang2.zighang.web.dto.tmap.response.TmapGeocodingResponseDto;
import zighang2.zighang.web.dto.tmap.response.TmapResponseDto;

import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class TmapClient {

    private final WebClient tmapWebClient;

    @Value("${tmap.coord-type}")
    private String coordType;

    //지오코딩(주소 -> 좌표)
    public GeocodePoint geocodeAddress(String address) {

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
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(),
                        resp -> resp.bodyToMono(String.class).defaultIfEmpty("")
                                .map(body -> new IllegalStateException(
                                        "Tmap geocoding 실패 (" + resp.statusCode() + "): " + body)))
                .bodyToMono(TmapGeocodingResponseDto.class)
                .map(dto -> {
                    if (dto == null
                            || dto.getCoordinateInfo() == null
                            || dto.getCoordinateInfo().getCoordinate() == null) {
                        log.error("지오코딩 응답이 null입니다. 주소: {}", address);
                        throw new IllegalStateException("지오코딩 응답이 비어 있습니다.");
                    }
                    var c = dto.getCoordinateInfo().getCoordinate().get(0);

                    String lat= c.getLat();
                    String lon= c.getLon();
                    if (lat == null || lat.isBlank() || lon == null || lon.isBlank()) {
                        String latEntr = c.getLatEntr();
                        String lonEntr = c.getLonEntr();
                        if (latEntr != null && !latEntr.isBlank() && lonEntr != null && !lonEntr.isBlank()) {
                            log.info("지오코딩된 주소(입구점 좌표): "+latEntr+lonEntr);
                            return new GeocodePoint(Double.parseDouble(c.getLat()), Double.parseDouble(c.getLon()));
                        } else {
                            throw new IllegalStateException("좌표를 찾을 수 없습니다.");
                        }
                    }
                    return new GeocodePoint(Double.parseDouble(lat), Double.parseDouble(lon));
                })
                .block();
    }

    //자동차 경로 시간 계산
    public Integer getDrivingDurationSeconds(GeocodePoint start, GeocodePoint end){
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
                .map(this::extractCarDurationSeconds)
                .block();

    }

    //대중교통 경로 시간 계산
    public Integer getTransitDurationSeconds(GeocodePoint start, GeocodePoint end){
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
                .map(this::extractTransitDurationSeconds)
                .block();
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
