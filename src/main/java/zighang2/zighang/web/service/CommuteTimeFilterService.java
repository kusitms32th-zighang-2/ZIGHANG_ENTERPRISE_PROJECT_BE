package zighang2.zighang.web.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import zighang2.zighang.global.config.TmapClient;
import zighang2.zighang.web.domain.user.enums.Transport;
import zighang2.zighang.web.dto.GeocodePoint;

@Service
@RequiredArgsConstructor
public class CommuteTimeFilterService {
    private final TmapClient tmapClient;

    public Mono<Integer> durationByAddress(Transport transport,String userAddr, String companyAddr){
        Mono<GeocodePoint> userGeo = tmapClient.geocodeAddress(userAddr);
        Mono<GeocodePoint> companyGeo = tmapClient.geocodeAddress(companyAddr);

        return Mono.zip(userGeo,companyGeo)
                .flatMap(tuple->{
                    return switch (transport){
                        case CAR -> tmapClient.getDrivingDurationSeconds(tuple.getT1(),tuple.getT2());
                        case TRANSIT -> tmapClient.getTransitDurationSeconds(tuple.getT1(),tuple.getT2());
                        default -> Mono.error(new IllegalArgumentException("Unknown transport"));
                    };
                });

    }

    public Mono<Integer> durationByGeocode(Transport transport,GeocodePoint userAddr, GeocodePoint companyAddr){

        return switch (transport){
            case CAR -> tmapClient.getDrivingDurationSeconds(userAddr,companyAddr);
            case TRANSIT -> tmapClient.getTransitDurationSeconds(userAddr,companyAddr);
            default -> Mono.error(new IllegalArgumentException("Unknown transport"));
        };
    }
}
