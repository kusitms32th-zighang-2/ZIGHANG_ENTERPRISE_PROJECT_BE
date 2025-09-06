package zighang2.zighang.web.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import zighang2.zighang.global.auth.jwt.JwtProvider;
import zighang2.zighang.global.config.TmapClient;
import zighang2.zighang.global.payload.code.status.ErrorStatus;
import zighang2.zighang.global.payload.exception.handler.NotFoundHandler;
import zighang2.zighang.web.domain.JobPosting;
import zighang2.zighang.web.domain.user.User;
import zighang2.zighang.web.domain.user.enums.Transport;
import zighang2.zighang.web.dto.JobPostingDto;
import zighang2.zighang.web.dto.tmap.GeocodePoint;
import zighang2.zighang.web.repository.JobPostingRepository;
import zighang2.zighang.web.repository.UserRepository;

import javax.swing.text.html.Option;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecommendService {

    private final TmapClient tmapClient;
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final JobPostingRepository jobPostingRepository;

    public List<JobPostingDto.JobPostingResponseDto> recommend6Posting() {
        User user = userRepository.findById(jwtProvider.getCurrentUserId())
                .orElseThrow(() -> new NotFoundHandler(ErrorStatus.USER_NOT_FOUND));

        Transport transport = user.getTransport();
        int maxMinutes = user.getMaxCommuteMinutes();
        Integer maxSeconds = maxMinutes * 60;
        String userAddr = user.getAddress();
        log.info("userAddr: {} maxSeconds: {} transport: {}", userAddr, maxSeconds, transport);

        List<JobPosting> candidates = jobPostingRepository.findTop50ByOrderByIdDesc();
        log.info("candidates: {}", candidates.size());

        GeocodePoint userGeo = tmapClient.geocodeAddress(userAddr);
        log.info("userGeo: {} {}", userGeo.lonAsString(), userGeo.latAsString());

        return candidates.stream()
                .map(job->{
                    try{
                        GeocodePoint companyGeo = tmapClient.geocodeAddress(job.getCompanyAddress());
                        log.info("companyGeo: {} {}", companyGeo.lonAsString(), companyGeo.latAsString());
                        int sec = switch (transport) {
                            case CAR     -> tmapClient.getDrivingDurationSeconds(userGeo, companyGeo);
                            case TRANSIT -> tmapClient.getTransitDurationSeconds(userGeo, companyGeo,maxMinutes);
                        };
                        log.info("sec: {}", sec);
                        return Optional.of(new AbstractMap.SimpleEntry<>(job, sec));
                        } catch (Exception e){
                        return Optional.<Map.Entry<JobPosting,Integer>>empty();
                    }
                })
                .flatMap(Optional::stream)
                .filter(pair->pair.getValue() <=maxSeconds)
                .sorted(Comparator.comparingInt(Map.Entry::getValue)) // 가까운 순 정렬
                .limit(6)
                .map(entry -> JobPostingDto.JobPostingResponseDto.of(entry.getKey()))
                .collect(Collectors.toList());

    }
}
