package zighang2.zighang.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import zighang2.zighang.global.utils.TimeFormatter;
import zighang2.zighang.web.dto.tmap.GeocodePoint;
import zighang2.zighang.web.dto.tmap.response.TmapGeocodingResponseDto;
import zighang2.zighang.web.dto.tmap.response.TmapResponseDto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
public class TmapClient {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private final WebClient tmapWebClient;
    private final TimeFormatter timeFormatter;

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
                        throw new IllegalStateException("지오코딩 응답이 비어 있습니다.");
                    }
                    var c = dto.getCoordinateInfo().getCoordinate().get(0);

                    String lat= c.getLat();
                    String lon= c.getLon();
                    if (lat == null || lat.isBlank() || lon == null || lon.isBlank()) {
                        String latEntr = c.getLatEntr();
                        String lonEntr = c.getLonEntr();
                        if (latEntr != null && !latEntr.isBlank() && lonEntr != null && !lonEntr.isBlank()) {
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
        String arrivalKst = timeFormatter.formatISOKST();

        Map<String,Object> departure = Map.of(
                "name","출발지",
                "lon",start.getLongitude(),
                "lat",start.getLatitude(),
                "depSearchFlag","03"
        );
        Map<String,Object> destination = Map.of(
                "name","도착지",
                "lon",end.getLongitude(),
                "lat",end.getLatitude(),
                "destSearchFlag","03"
        );

        Map<String, Object> routesInfo = new HashMap<>();

        routesInfo.put("departure", departure);
        routesInfo.put("destination", destination);
        routesInfo.put("predictionType", "departure");
        routesInfo.put("predictionTime", arrivalKst);
        routesInfo.put("trafficInfo","Y");
        routesInfo.put("searchOption","00");

        return tmapWebClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("tmap/routes/prediction")
                        .queryParam("version", 1)
                        .queryParam("format", "json")
                        .queryParam("resCoordType", coordType)
                        .queryParam("reqCoordType", coordType)
                        .queryParam("sort", "index")
                        .queryParam("totalValue",2)
                        .build())
                .bodyValue(Map.of("routesInfo", routesInfo))
                .retrieve()
                .bodyToMono(TmapResponseDto.TmapRouteResDto.class)
                .map(this::extractCarDurationSeconds)
                .block();

    }

    //대중교통 경로 시간 계산
    public Integer getTransitDurationSeconds(GeocodePoint start, GeocodePoint end, int maxCommuteMinutes){
        String departureKst = timeFormatter.formatDTTM(computeDepartureForArriveAt9(maxCommuteMinutes));

        Map<String, Object> body = new HashMap<>();
        body.put("startX", start.lonAsString());
        body.put("startY", start.latAsString());
        body.put("endX", end.lonAsString());
        body.put("endY", end.latAsString());
        body.put("format", "json");
        body.put("searchDttm", departureKst);

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
        if (res == null || res.getMetaData() == null ||
                res.getMetaData().getPlan() == null ||
                res.getMetaData().getPlan().getItineraries() == null ||
                res.getMetaData().getPlan().getItineraries().isEmpty()) {

            throw new IllegalStateException("대중교통 요약 응답에 itineraries가 없습니다. (경로 없음/매핑 실패 가능)");
        }

        return res.getMetaData().getPlan().getItineraries().stream()
                .map(TmapResponseDto.TmapTransitResDto.Itinerary::getTotalTime)
                .filter(Objects::nonNull)
                .min(Integer::compareTo)
                .orElseThrow(() -> new IllegalStateException("대중교통 요약 응답에서 totalTime을 찾지 못했습니다."));
    }

    private ZonedDateTime computeDepartureForArriveAt9(int maxCommuteMinutes) {
        ZonedDateTime now = ZonedDateTime.now(KST);
        LocalDate targetDate = now.toLocalTime().isBefore(LocalTime.of(9, 0))
                ? now.toLocalDate()
                : now.toLocalDate().plusDays(1);
        ZonedDateTime arriveAt9 = ZonedDateTime.of(targetDate, LocalTime.of(9, 0), KST);
        return arriveAt9.minusMinutes(maxCommuteMinutes);
    }

}
