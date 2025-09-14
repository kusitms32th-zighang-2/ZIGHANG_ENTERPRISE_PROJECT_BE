package zighang2.zighang.web.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.*;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zighang2.zighang.global.auth.jwt.JwtProvider;
import zighang2.zighang.global.config.TmapClient;
import zighang2.zighang.global.payload.code.status.ErrorStatus;
import zighang2.zighang.global.payload.exception.handler.BadRequestHandler;
import zighang2.zighang.global.payload.exception.handler.NotFoundHandler;
import zighang2.zighang.web.domain.*;
import zighang2.zighang.web.domain.enums.*;
import zighang2.zighang.web.domain.user.User;
import zighang2.zighang.web.dto.JobPostingResponseDto;
import zighang2.zighang.web.dto.JobRecommendDto;
import zighang2.zighang.web.dto.tmap.GeocodePoint;
import zighang2.zighang.web.repository.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendService {

    private final TmapClient tmapClient;
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final JobRecommendRepository jobRecommendRepository;
    private final RecruitmentTypeRepository recruitmentTypeRepository;
    private final RestHighLevelClient client;
    private final EmbeddingService embeddingService;
    private final JobPostingRecruitmentTypeRepository jobPostingRecruitmentTypeRepository;
    private final JobGroupRepository jobGroupRepository;
    private final JobPositionRepository jobPositionRepository;

    public List<JobRecommendDto.JobRecommendResponseDto> get6Recommends() {
        User user = userRepository.findById(jwtProvider.getCurrentUserId())
                .orElseThrow(() -> new NotFoundHandler(ErrorStatus.USER_NOT_FOUND));

        validateUserPreferences(user);

        List<JobRecommend> candidates = jobRecommendRepository.findTop10ByOrderByIdDesc();
        List<Map.Entry<JobRecommend, Integer>> jobsWithCommuteTimes = calculateJobCommuteTimes(candidates, user);

        return jobsWithCommuteTimes.stream()
                .filter(entry -> entry.getValue() <= user.getMaxCommuteMinutes() * 60)
                .sorted(Comparator.comparingInt(Map.Entry::getValue))
                .limit(6)
                .map(entry -> JobRecommendDto.JobRecommendResponseDto.of(entry.getKey()))
                .collect(Collectors.toList());
    }

    private void validateUserPreferences(User user) {
        if(user.getAddress() == null || user.getAddress().isEmpty()) {
            throw new NotFoundHandler(ErrorStatus.ADDRESS_NOT_FOUND);
        }
        if (user.getTransport() == null){
            throw new NotFoundHandler(ErrorStatus.TRANSPORT_NOT_FOUND);
        }
        if (user.getMaxCommuteMinutes() == null){
            throw new NotFoundHandler(ErrorStatus.MAXCOMMUTE_NOT_FOUND);
        }
    }

    public List<Map.Entry<JobRecommend, Integer>> calculateJobCommuteTimes(List<JobRecommend> jobs, User user) {
        GeocodePoint userLocation = tmapClient.geocodeAddress(user.getAddress());
        Transport transport = user.getTransport();
        int maxMinutes = user.getMaxCommuteMinutes();

        return jobs.stream()
                .map(job -> calculateSingleJobCommuteTime(job, userLocation, transport, maxMinutes))
                .flatMap(Optional::stream)
                .map(entry -> {
                    JobRecommend job = entry.getKey();
                    int commuteMinutes = (entry.getValue() + 59) / 60;
                    job.setCommuteMinutes(commuteMinutes);
                    return new AbstractMap.SimpleEntry<>(job, commuteMinutes);
                })
                .filter(entry->{
                    int commuteMinutes = entry.getValue();
                    return commuteMinutes <= maxMinutes;
                })
                .collect(Collectors.toList());
    }

    private Optional<Map.Entry<JobRecommend, Integer>> calculateSingleJobCommuteTime(JobRecommend job,
                                                                                     GeocodePoint userLocation, Transport transport, int maxMinutes) {
        try {
            GeocodePoint companyLocation = tmapClient.geocodeAddress(job.getRecruitmentAddress());
            int commuteSeconds = switch (transport) {
                case CAR -> tmapClient.getDrivingDurationSeconds(userLocation, companyLocation);
                case TRANSIT -> tmapClient.getTransitDurationSeconds(userLocation, companyLocation, maxMinutes);
                default -> throw new BadRequestHandler(ErrorStatus.INVALID_TRANSPORT);
            };
            return Optional.of(new AbstractMap.SimpleEntry<>(job, commuteSeconds));
        } catch (Exception e) {
            return Optional.empty();
        }
    }


    @Async
    public void getFullRecommendationsAsync(User user,
                                            List<String> welfareList,
                                            Map<String, Double> companyRatio) {
        try {
            float[] welfareEmbedding = embeddingService.getEmbedding(welfareList);
            String queryJson = buildQuery(user, welfareEmbedding);

            // openSearch 요청
            Request request = new Request("POST", "/job-postings/_search");
            request.setJsonEntity(queryJson);

            Response response = client.getLowLevelClient().performRequest(request);

            // 응답 변환
            String responseBody = EntityUtils.toString(response.getEntity());

            SearchResponse searchResponse = SearchResponse.fromXContent(
                    JsonXContent.jsonXContent.createParser(
                            NamedXContentRegistry.EMPTY,
                            DeprecationHandler.THROW_UNSUPPORTED_OPERATION,
                            responseBody
                    )
            );

            // 후보군 파싱
            List<JobRecommend> candidates = Arrays.stream(searchResponse.getHits().getHits())
                    .map(hit -> parseJobPosting(hit.getSourceAsMap()))
                    .toList();

            // ================= 거리 필터링 =========================
            List<Map.Entry<JobRecommend, Integer>> commuteFilteredEntries = this.calculateJobCommuteTimes(candidates, user);
            List<JobRecommend> filteredCandidates = commuteFilteredEntries.stream()
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            // =====================================================

            List<JobRecommend> distributed = distributeByCompanyRatio(filteredCandidates, companyRatio, 100);

            // User 연관관계 설정
            distributed.forEach(job -> job.setUser(user));

            // JobRecommend 저장
            jobRecommendRepository.saveAll(distributed);

            // JobPostingRecruitmentType 저장
            distributed.forEach(job -> {
                if (!job.getJobPostingRecruitmentTypes().isEmpty()) {
                    jobPostingRecruitmentTypeRepository.saveAll(job.getJobPostingRecruitmentTypes());
                }
            });


            log.info("Full Recommendations 저장 완료: {}개", distributed.size());


        } catch (Exception e) {
            e.printStackTrace();
            log.error("getFullRecommendationsAsync 실패: {}", e.getMessage());
        }
    }

    public List<JobRecommend> getQuickRecommendations(User user,
                                                      List<String> welfareList) {
        try {
            float[] welfareEmbedding = embeddingService.getEmbedding(welfareList);
            String queryJson = buildQuery(user, welfareEmbedding);
            System.out.println("queryJson = " + queryJson);

            // openSearch 요청
            Request request = new Request("POST", "/job-postings/_search");
            request.setJsonEntity(queryJson);

            Response response = client.getLowLevelClient().performRequest(request);

            // 응답 변환
            String responseBody = EntityUtils.toString(response.getEntity());

            SearchResponse searchResponse = SearchResponse.fromXContent(
                    JsonXContent.jsonXContent.createParser(
                            NamedXContentRegistry.EMPTY,
                            DeprecationHandler.THROW_UNSUPPORTED_OPERATION,
                            responseBody
                    )
            );

            // 후보군 파싱
            List<JobRecommend> candidates = Arrays.stream(searchResponse.getHits().getHits())
                    .map(hit -> parseJobPosting(hit.getSourceAsMap()))
                    .toList();

            System.out.println("candidates = " + candidates);
            // 출력
            return candidates;

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    // openSearch 쿼리 생성 메서드
    private static String buildQuery(User user, float[] welfareEmbedding) throws IOException {
        XContentBuilder builder = XContentFactory.jsonBuilder();
        builder.startObject();
        {
            builder.field("size", 200);

            // query
            builder.startObject("query");
            {
                builder.startObject("bool");
                {
                    // filter 조건
                    builder.startArray("filter");
                    {
                        // depthOne → term (직군 하나)
                        builder.startObject();
                        {
                            builder.startObject("terms")
                                    .field("depthOne", List.of(user.getJobGroup().getJobGroupName()))
                                    .endObject();
                        }
                        builder.endObject();

                        // depthTwo → terms (직무 여러개)
                        builder.startObject();
                        {
                            builder.startObject("terms")
                                    .field("depthTwo",
                                            user.getUserJobPositions().stream()
                                                    .map(pos -> pos.getJobPosition().getJobPositionName().getDisplay())
                                                    .toList())
                                    .endObject();
                        }
                        builder.endObject();

                        // educationLevel → range
                        builder.startObject();
                        {
                            builder.startObject("range");
                            {
                                builder.startObject("educationLevel")
                                        .field("lte", user.getEducation().getLevel())
                                        .endObject();
                            }
                            builder.endObject();
                        }
                        builder.endObject();

                        // career → range
                        builder.startObject();
                        {
                            builder.startObject("range");
                            {
                                builder.startObject("career")
                                        .field("lte", user.getWorkExperience())
                                        .endObject();
                            }
                            builder.endObject();
                        }
                        builder.endObject();
                    }
                    builder.endArray();

                    // must → knn 검색
                    builder.startArray("must");
                    {
                        builder.startObject();
                        {
                            builder.startObject("knn");
                            {
                                builder.startObject("embedding");
                                {
                                    builder.field("vector", welfareEmbedding);
                                    builder.field("k", 200 );
                                }
                                builder.endObject();
                            }
                            builder.endObject();
                        }
                        builder.endObject();
                    }
                    builder.endArray();
                }
                builder.endObject();
            }
            builder.endObject();
        }
        builder.endObject();

        return Strings.toString(builder);
    }


    // 응답 파싱 후 JobRecommend 매핑 -> JobPostingRecruitmentType 매핑 후 객체 그래프 생성 메서드
    @SuppressWarnings("unchecked")
    private JobRecommend parseJobPosting(Map<String, Object> source) {
        Object companyObj = source.get("company");
        Map<String, Object> company = null;

        try {
            if (companyObj instanceof Map) {
                company = (Map<String, Object>) companyObj;
            } else if (companyObj instanceof String) {
                company = new ObjectMapper().readValue((String) companyObj, Map.class);
            }
        } catch (Exception e) {
            log.warn("Failed to parse company field: {}", companyObj, e);
        }

        // career 처리
        String workExperience = getString(source.get("career"));

        // welfare_list 처리
        String welfare = getString(source.get("welfare_list"));

        JobRecommend jobRecommend = JobRecommend.builder()
                .workExperience(workExperience)
                .welfare(welfare)
                .title((String) source.getOrDefault("title", ""))
                .companyName(company != null ? (String) company.getOrDefault("companyName", "") : "")
                .recruitmentAddress((String) source.getOrDefault("recruitmentAddress", ""))
                .ocrData((String) source.getOrDefault("ocrData", ""))
                .content((String) source.getOrDefault("content", ""))
                .companyType(company != null && company.get("companyType") != null
                        ? CompanyTypeEnum.valueOf(company.get("companyType").toString())
                        : null)
                .education(source.get("education") != null
                        ? Education.valueOf(getFirstValue(source.get("education")))
                        : null)
                .build();


        Object recruitmentTypeObj = source.get("recruitmentType");
        if (recruitmentTypeObj instanceof List) {
            List<?> recruitmentList = (List<?>) recruitmentTypeObj;
            // 중복 제거
            Set<String> recruitmentSet = recruitmentList.stream()
                    .map(Object::toString)
                    .collect(Collectors.toSet());

            for (String r : recruitmentSet) {
                try {
                    RecruitmentTypeEnum typeEnum = RecruitmentTypeEnum.valueOf(r);
                    RecruitmentType recruitmentType = recruitmentTypeRepository.findByRecruitmentType(typeEnum)
                            .orElseThrow(() -> new NotFoundHandler(ErrorStatus.RECRUITMENT_TYPE_NOT_FOUND));

                    JobPostingRecruitmentType jobPostingRecruitmentType = JobPostingRecruitmentType.builder()
                            .jobRecommend(jobRecommend)
                            .recruitmentType(recruitmentType)
                            .build();

                    jobRecommend.getJobPostingRecruitmentTypes().add(jobPostingRecruitmentType);
                } catch (IllegalArgumentException e) {
                    log.warn("Unknown recruitmentType value: {}", r);
                }
            }
        }

        Object jobGroupsObj = source.get("depthOne");
        System.out.println("jobGroupsObj = " + jobGroupsObj);
        if (jobGroupsObj instanceof List<?> jobGroupList) {
            Set<String> groupNames = jobGroupList.stream()
                    .map(Object::toString)
                    .collect(Collectors.toSet());

            for (String g : groupNames) {
                JobGroupEnum.from(g).ifPresentOrElse(
                        groupEnum -> {
                            JobGroup jobGroup = jobGroupRepository.findByJobGroupName(groupEnum)
                                    .orElseThrow(() -> new NotFoundHandler(ErrorStatus.JOBGROUP_NOT_FOUND));

                            JobPostingJobGroup jobPostingJobGroup = JobPostingJobGroup.builder()
                                    .jobRecommend(jobRecommend)
                                    .jobGroup(jobGroup)
                                    .build();

                            jobRecommend.getJobPostingJobGroups().add(jobPostingJobGroup);
                            }, () -> log.warn("Unknown or invalid jobGroup value: '{}'. Skipping.", g)
                );
            }
        }

        Object jobPositionsObj = source.get("depthTwo");
        System.out.println("jobPositionsObj = " + jobPositionsObj);
        if (jobPositionsObj instanceof List<?> jobPositionList) {
            Set<String> posNames = jobPositionList.stream()
                    .map(Object::toString)
                    .map(this::normalizeJobPositionName)
                    .collect(Collectors.toSet());

            for (String p : posNames) {
                JobPositionEnum.from(p).ifPresentOrElse(
                        posEnum -> jobPositionRepository.findByJobPositionName(posEnum)
                                .ifPresentOrElse(
                                        jobPosition -> {
                                            JobPostingJobPosition jobPostingJobPosition = JobPostingJobPosition.builder()
                                                    .jobRecommend(jobRecommend)
                                                    .jobPosition(jobPosition)
                                                    .build();
                                            jobRecommend.getJobPostingJobPositions().add(jobPostingJobPosition);
                                        },
                                        () -> log.warn("JobPositionEnum '{}' 은 있지만 DB에 존재하지 않음. Skipping.", posEnum)
                                ),
                        () -> log.warn("Unknown or invalid jobPosition value: '{}'. Skipping.", p)
                );
            }
        }

        return jobRecommend;
    }

    private String getString(Object obj) {
        if (obj instanceof List<?>) {
            return ((List<?>) obj).stream()
                    .map(Object::toString)
                    .collect(Collectors.joining("/"));
        } else if (obj != null) {
            return obj.toString();
        }
        return "";
    }

    // 회사 유형별 비율 조절 메서드
    private List<JobRecommend> distributeByCompanyRatio(List<JobRecommend> candidates,
                                                        Map<String, Double> ratio,
                                                        int totalCount) {
        // 후보군을 회사 유형별로 그룹핑
        Map<CompanyTypeEnum, List<JobRecommend>> grouped = candidates.stream()
                .filter(j -> j.getCompanyType() != null)
                .collect(Collectors.groupingBy(JobRecommend::getCompanyType));

        List<JobRecommend> result = new ArrayList<>();

        for (Map.Entry<String, Double> entry : ratio.entrySet()) {
            try {
                CompanyTypeEnum type = CompanyTypeEnum.valueOf(entry.getKey());
                double percent = entry.getValue();

                int count = (int) Math.round(totalCount * percent);
                List<JobRecommend> posts = grouped.getOrDefault(type, Collections.emptyList());

                result.addAll(posts.stream()
                        .limit(count)
                        .toList());
            } catch (IllegalArgumentException e) {
                // ratio key가 enum에 없는 경우 무시 (예: "all")
            }
        }
        return result;
    }

    private String getFirstValue(Object value) {
        if (value == null) return null;
        if (value instanceof java.util.List) {
            List<?> list = (List<?>) value;
            return list.isEmpty() ? null : list.get(0).toString();
        }
        return value.toString();
    }

    private String normalizeJobPositionName(String raw) {
        if (raw == null) {
            return null;
        }
        // 앞뒤 공백 제거
        String trimmed = raw.trim();
        String replaced = trimmed.replaceAll("·+", "_");
        return replaced;
    }

    @Transactional(readOnly = true)
    public JobPostingResponseDto.JobPostingDetailDto getJobPostingDetail(Long jobPostingId){
        JobRecommend jobRecommend = jobRecommendRepository.findById(jobPostingId)
                .orElseThrow(()-> new NotFoundHandler(ErrorStatus.JOBRECOMMEND_NOT_FOUND));

        return JobPostingResponseDto.JobPostingDetailDto.of(jobRecommend);
    }

    @Transactional(readOnly = true)
    public JobPostingResponseDto.JobPostingListWrapper getJobPostings(Long lastId) {
        User user = userRepository.findById(jwtProvider.getCurrentUserId())
                .orElseThrow(() -> new NotFoundHandler(ErrorStatus.USER_NOT_FOUND));

        List<JobRecommend> jobs;

        if (lastId == null) {
            // 첫 로딩: 최신 10개
            jobs = jobRecommendRepository.findTop10ByUserOrderByIdDesc(user);
        } else {
            // lastId보다 작은 id 10개
            jobs = jobRecommendRepository.findTop10ByUserAndIdLessThanOrderByIdDesc(user, lastId);
        }

        List<JobPostingResponseDto.JobPostingListDto> jobDtos = jobs.stream()
                .map(job -> JobPostingResponseDto.JobPostingListDto.builder()
                        .jobPostingId(job.getId())
                        .companyName(job.getCompanyName())
                        .jobPostingTitle(job.getTitle())
                        .workExperience(formatWorkExperience(job.getWorkExperience()))
                        .recruitmentType(
                                job.getJobPostingRecruitmentTypes().stream()
                                        .map(rt -> rt.getRecruitmentType().getRecruitmentType().name())
                                        .toList()
                        )
                        .education(job.getEducation() != null ? job.getEducation().name() : null)
                        .commuteMinutes(job.getCommuteMinutes())
                        .welfare(job.getWelfare())
                        .build()
                )
                .toList();

        boolean hasNext = jobs.size() == 10;

        return JobPostingResponseDto.JobPostingListWrapper.builder()
                .jobs(jobDtos)
                .hasNext(hasNext)
                .build();


    }

    private String formatWorkExperience(String workExpRaw) {
        if (workExpRaw == null || workExpRaw.isBlank()) {
            return null;
        }

        List<Integer> values = Arrays.stream(workExpRaw.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .toList();

        List<String> result = new ArrayList<>();

        if (values.contains(-1)) {
            result.add("경력무관");
        }
        if (values.contains(0)) {
            result.add("신입");
        }

        List<Integer> positives = values.stream()
                .filter(v -> v > 0)
                .sorted()
                .toList();

        if (!positives.isEmpty()) {
            int min = positives.get(0);
            int max = positives.get(positives.size() - 1);
            if (min == max) {
                result.add(min + "년 이상");
            } else {
                result.add(min + "~" + max + "년");
            }
        }

        return String.join("/", result);
    }
}
