package zighang2.zighang.global.config;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import zighang2.zighang.global.payload.code.status.ErrorStatus;
import zighang2.zighang.global.payload.exception.handler.BadRequestHandler;
import zighang2.zighang.global.payload.exception.handler.NotFoundHandler;
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

    // 지오코딩(주소 -> 좌표)
    public GeocodePoint geocodeAddress(String address) {
        TmapGeocodingResponseDto response = callGeocodingApi(address);
        return parseGeocodeResponse(response);
    }

    // 자동차 경로 시간 계산
    public Integer getDrivingDurationSeconds(GeocodePoint start, GeocodePoint end) {
        TmapResponseDto.TmapRouteResDto response = callDrivingRouteApi(start, end);
        return extractCarDurationSeconds(response);
    }

    // 대중교통 경로 시간 계산
    public Integer getTransitDurationSeconds(GeocodePoint start, GeocodePoint end, int maxCommuteMinutes) {
        TmapResponseDto.TmapTransitResDto response = callTransitRouteApi(start, end, maxCommuteMinutes);
        return extractTransitDurationSeconds(response);
    }

    //fullText geocoding api
    private TmapGeocodingResponseDto callGeocodingApi(String address) {
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
                        resp -> resp.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .map(body -> new BadRequestHandler(ErrorStatus.TMAP_GEOCODING_MAPPING_FAILED)))
                .bodyToMono(TmapGeocodingResponseDto.class)
                .block();
    }

    //타임머신 자동차 길 안내 api
    private TmapResponseDto.TmapRouteResDto callDrivingRouteApi(GeocodePoint start, GeocodePoint end) {
        Map<String, Object> routesInfo = buildDrivingRouteRequest(start, end);

        return tmapWebClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("tmap/routes/prediction")
                        .queryParam("version", 1)
                        .queryParam("format", "json")
                        .queryParam("resCoordType", coordType)
                        .queryParam("reqCoordType", coordType)
                        .queryParam("sort", "index")
                        .queryParam("totalValue", 2)
                        .build())
                .bodyValue(Map.of("routesInfo", routesInfo))
                .retrieve()
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(),
                        resp -> resp.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .map(body -> new BadRequestHandler(ErrorStatus.TMAP_DRIVING_MAPPING_FAILED)))
                .bodyToMono(TmapResponseDto.TmapRouteResDto.class)
                .block();
    }

    //대중교통 요약 api
    private TmapResponseDto.TmapTransitResDto callTransitRouteApi(GeocodePoint start, GeocodePoint end, int maxCommuteMinutes) {
        Map<String, Object> requestBody = buildTransitRouteRequest(start, end, maxCommuteMinutes);

        return tmapWebClient.post()
                .uri("transit/routes/sub")
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(),
                        resp -> resp.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .map(body -> new BadRequestHandler(ErrorStatus.TMAP_TRANSIT_MAPPING_FAILED)))
                .bodyToMono(TmapResponseDto.TmapTransitResDto.class)
                .block();
    }


    private Map<String, Object> buildDrivingRouteRequest(GeocodePoint start, GeocodePoint end) {
        String arrivalKst = timeFormatter.formatISOKST();

        Map<String, Object> departure = Map.of(
                "name", "출발지",
                "lon", start.getLongitude(),
                "lat", start.getLatitude(),
                "depSearchFlag", "03"
        );

        Map<String, Object> destination = Map.of(
                "name", "도착지",
                "lon", end.getLongitude(),
                "lat", end.getLatitude(),
                "destSearchFlag", "03"
        );

        Map<String, Object> routesInfo = new HashMap<>();
        routesInfo.put("departure", departure);
        routesInfo.put("destination", destination);
        routesInfo.put("predictionType", "departure");
        routesInfo.put("predictionTime", arrivalKst);
        routesInfo.put("trafficInfo", "Y");
        routesInfo.put("searchOption", "00");

        return routesInfo;
    }

    private Map<String, Object> buildTransitRouteRequest(GeocodePoint start, GeocodePoint end, int maxCommuteMinutes) {
        String departureKst = timeFormatter.formatDTTM(computeDepartureForArriveAt9(maxCommuteMinutes));

        Map<String, Object> body = new HashMap<>();
        body.put("startX", start.lonAsString());
        body.put("startY", start.latAsString());
        body.put("endX", end.lonAsString());
        body.put("endY", end.latAsString());
        body.put("format", "json");
        body.put("searchDttm", departureKst);

        return body;
    }


    private GeocodePoint parseGeocodeResponse(TmapGeocodingResponseDto dto) {
        validateGeocodeResponse(dto);

        var coordinate = dto.getCoordinateInfo().getCoordinate().get(0);
        String lat = coordinate.getLat();
        String lon = coordinate.getLon();

        if (isBlankCoordinate(lat, lon)) {
            return tryParseEntrCoordinate(coordinate);
        }

        return new GeocodePoint(Double.parseDouble(lat), Double.parseDouble(lon));
    }

    private void validateGeocodeResponse(TmapGeocodingResponseDto dto) {
        if (dto == null || dto.getCoordinateInfo() == null ||
                dto.getCoordinateInfo().getCoordinate() == null || dto.getCoordinateInfo().getCoordinate().isEmpty()) {
            throw new NotFoundHandler(ErrorStatus.TMAP_DRIVING_EMPTY);
        }
    }

    private boolean isBlankCoordinate(String lat, String lon) {
        return lat == null || lat.isBlank() || lon == null || lon.isBlank();
    }

    private GeocodePoint tryParseEntrCoordinate(TmapGeocodingResponseDto.Coordinate coordinate) {
        String latEntr = coordinate.getLatEntr();
        String lonEntr = coordinate.getLonEntr();

        if (latEntr != null && !latEntr.isBlank() && lonEntr != null && !lonEntr.isBlank()) {
            return new GeocodePoint(Double.parseDouble(latEntr), Double.parseDouble(lonEntr));
        }

        throw new NotFoundHandler(ErrorStatus.TMAP_COORDINATE_NOT_FOUND);
    }


    private Integer extractCarDurationSeconds(TmapResponseDto.TmapRouteResDto res) {
        if (res == null || res.getFeatures() == null || res.getFeatures().isEmpty()) {
            throw new NotFoundHandler(ErrorStatus.TMAP_DRIVING_EMPTY);}

        return res.getFeatures().stream()
                .map(TmapResponseDto.TmapRouteResDto.Feature::getProperties)
                .filter(Objects::nonNull)
                .map(TmapResponseDto.TmapRouteResDto.Properties::getTotalTime)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElseThrow(() -> new BadRequestHandler(ErrorStatus.TMAP_DRIVING_MAPPING_FAILED));
    }

    private Integer extractTransitDurationSeconds(TmapResponseDto.TmapTransitResDto res) {
        validateTransitResponse(res);

        return res.getMetaData().getPlan().getItineraries().stream()
                .map(TmapResponseDto.TmapTransitResDto.Itinerary::getTotalTime)
                .filter(Objects::nonNull)
                .min(Integer::compareTo)
                .orElseThrow(() -> new BadRequestHandler(ErrorStatus.TMAP_TRANSIT_MAPPING_FAILED));
    }

    private void validateTransitResponse(TmapResponseDto.TmapTransitResDto res) {
        if (res == null || res.getMetaData() == null ||
                res.getMetaData().getPlan() == null ||
                res.getMetaData().getPlan().getItineraries() == null ||
                res.getMetaData().getPlan().getItineraries().isEmpty()) {
            throw new NotFoundHandler(ErrorStatus.TMAP_TRANSIT_EMPTY);
        }
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
