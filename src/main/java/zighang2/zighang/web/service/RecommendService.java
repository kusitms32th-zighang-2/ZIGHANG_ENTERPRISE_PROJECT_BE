package zighang2.zighang.web.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zighang2.zighang.global.auth.jwt.JwtProvider;
import zighang2.zighang.global.config.TmapClient;
import zighang2.zighang.global.payload.code.status.ErrorStatus;
import zighang2.zighang.global.payload.exception.handler.NotFoundHandler;
import zighang2.zighang.web.domain.JobRecommend;
import zighang2.zighang.web.domain.enums.Transport;
import zighang2.zighang.web.domain.user.User;
import zighang2.zighang.web.dto.JobRecommendDto;
import zighang2.zighang.web.dto.tmap.GeocodePoint;
import zighang2.zighang.web.repository.JobPostingRepository;
import zighang2.zighang.web.repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendService {

    private final TmapClient tmapClient;
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final JobPostingRepository jobPostingRepository;

    public List<JobRecommendDto.JobRecommendResponseDto> recommend6Posting() {
        User user = userRepository.findById(jwtProvider.getCurrentUserId())
                .orElseThrow(() -> new NotFoundHandler(ErrorStatus.USER_NOT_FOUND));

        List<JobRecommend> candidates = jobPostingRepository.findTop10ByOrderByIdDesc();
        List<Map.Entry<JobRecommend, Integer>> jobsWithCommuteTimes = calculateJobCommuteTimes(candidates, user);

        return jobsWithCommuteTimes.stream()
                .filter(entry -> entry.getValue() <= user.getMaxCommuteMinutes() * 60)
                .sorted(Comparator.comparingInt(Map.Entry::getValue))
                .limit(6)
                .map(entry -> JobRecommendDto.JobRecommendResponseDto.of(entry.getKey()))
                .collect(Collectors.toList());
    }

    public List<Map.Entry<JobRecommend, Integer>> calculateJobCommuteTimes(List<JobRecommend> jobs, User user) {
        GeocodePoint userLocation = tmapClient.geocodeAddress(user.getAddress());
        Transport transport = user.getTransport();
        int maxMinutes = user.getMaxCommuteMinutes();

        return jobs.stream()
                .map(job -> calculateSingleJobCommuteTime(job, userLocation, transport, maxMinutes))
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    private Optional<Map.Entry<JobRecommend, Integer>> calculateSingleJobCommuteTime(JobRecommend job,
                                                                                     GeocodePoint userLocation, Transport transport,
                                                                                     int maxMinutes) {
        try {
            GeocodePoint companyLocation = tmapClient.geocodeAddress(job.getRecruitmentAddress());
            int commuteSeconds = switch (transport) {
                case CAR -> tmapClient.getDrivingDurationSeconds(userLocation, companyLocation);
                case TRANSIT -> tmapClient.getTransitDurationSeconds(userLocation, companyLocation, maxMinutes);
            };
            return Optional.of(new AbstractMap.SimpleEntry<>(job, commuteSeconds));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
