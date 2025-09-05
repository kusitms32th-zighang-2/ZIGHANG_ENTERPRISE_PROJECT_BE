package zighang2.zighang.global.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import zighang2.zighang.web.dto.GeocodePoint;
import zighang2.zighang.web.dto.TmapGeocodingResponseDto;

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
                        .path("geo/fulAddrGeo")
                        .queryParam("version", 1)
                        .queryParam("format", "json")
                        .queryParam("fullAddr", address)
                        .queryParam("coordType", "WGS84GEO")
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

}
