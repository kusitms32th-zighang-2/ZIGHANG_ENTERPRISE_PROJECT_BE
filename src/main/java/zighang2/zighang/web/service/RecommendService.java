package zighang2.zighang.web.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.xcontent.*;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import zighang2.zighang.global.auth.jwt.JwtProvider;
import zighang2.zighang.global.config.TmapClient;
import zighang2.zighang.global.payload.code.status.ErrorStatus;
import zighang2.zighang.global.payload.exception.handler.BadRequestHandler;
import zighang2.zighang.global.payload.exception.handler.NotFoundHandler;
import zighang2.zighang.web.domain.JobRecommend;
import zighang2.zighang.web.domain.enums.CompanyTypeEnum;
import zighang2.zighang.web.domain.enums.Education;
import zighang2.zighang.web.domain.enums.RecruitmentType;
import zighang2.zighang.web.domain.enums.Transport;
import zighang2.zighang.web.domain.user.User;
import zighang2.zighang.web.dto.JobRecommendDto;
import zighang2.zighang.web.dto.tmap.GeocodePoint;
import zighang2.zighang.web.repository.JobRecommendRepository;
import zighang2.zighang.web.repository.UserRepository;

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


    private final RestHighLevelClient client;
    private final EmbeddingService embeddingService;

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
                default -> throw new BadRequestHandler(ErrorStatus.INVALID_TRANSPORT);
            };
            return Optional.of(new AbstractMap.SimpleEntry<>(job, commuteSeconds));
        } catch (Exception e) {
            return Optional.empty();
        }
    }


    public List<JobRecommend> getQuickRecommendations(User user,
                                                      List<String> welfareList,
                                                      Map<String, Double> companyRatio) {
        try {
            // 1. welfareList → embedding vector
            float[] welfareEmbedding = embeddingService.getEmbedding(welfareList);
            System.out.println("welfareEmbedding = " + welfareEmbedding);

            // 2. JSON DSL 직접 구성
            XContentBuilder builder = XContentFactory.jsonBuilder();
            builder.startObject();   // ✅ 루트 열기
            {
                builder.field("size", 200);

                // query
                builder.startObject("query");
                {
                    builder.startObject("bool");
                    {
//                        // filter 조건
//                        builder.startArray("filter");
//                        {
//                            // depthOne → terms_set
//                            builder.startObject();
//                            {
//                                builder.startObject("terms_set");
//                                {
//                                    builder.startObject("depthOne");
//                                    {
//                                        builder.field("terms", List.of(user.getJobGroup().getJobGroupName()));
//                                        builder.startObject("minimum_should_match_script")
//                                                .field("source", "1")
//                                                .endObject();
//                                    }
//                                    builder.endObject();
//                                }
//                                builder.endObject();
//                            }
//                            builder.endObject();
//
//                            // depthTwo → terms_set
//                            builder.startObject();
//                            {
//                                builder.startObject("terms_set");
//                                {
//                                    builder.startObject("depthTwo");
//                                    {
//                                        builder.field("terms", user.getUserJobPositions().stream()
//                                                .map(pos -> pos.getJobPosition().getJobPositionName())
//                                                .toList());
//                                        builder.startObject("minimum_should_match_script")
//                                                .field("source", "1")
//                                                .endObject();
//                                    }
//                                    builder.endObject();
//                                }
//                                builder.endObject();
//                            }
//                            builder.endObject();
//
////                            // education
////                            builder.startObject();
////                            {
////                                builder.startObject("range");
////                                {
////                                    builder.startObject("educationLevel")
////                                            .field("lte", user.getEducation().getLevel())
////                                            .endObject();
////                                }
////                                builder.endObject();
////                            }
////                            builder.endObject();
//
////                            // career
////                            builder.startObject();
////                            {
////                                builder.startObject("range");
////                                {
////                                    builder.startObject("career")
////                                            .field("lte", user.getWorkExperience())
////                                            .endObject();
////                                }
////                                builder.endObject();
////                            }
////                            builder.endObject();
//                        }
////                        builder.endArray();

                        // must → knn
                        builder.startArray("must");
                        {
                            builder.startObject();
                            {
                                builder.startObject("knn");
                                {
                                    builder.startObject("embedding");
                                    {
                                        builder.field("vector", welfareEmbedding);
                                        builder.field("k", 10 );
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
                builder.endObject(); // query
            }
            builder.endObject(); // ✅ 루트 닫기

            // 3. JSON 직렬화
            String queryJson = Strings.toString(builder);

            // 4. LowLevelClient 요청
            Request request = new Request("POST", "/job-postings/_search");
            request.setJsonEntity(queryJson);

            Response response = client.getLowLevelClient().performRequest(request);

            

            // 5. 응답 변환
            String responseBody = EntityUtils.toString(response.getEntity());

            SearchResponse searchResponse = SearchResponse.fromXContent(
                    JsonXContent.jsonXContent.createParser(
                            NamedXContentRegistry.EMPTY,
                            DeprecationHandler.THROW_UNSUPPORTED_OPERATION,
                            responseBody
                    )
            );

            System.out.println("searchResponse = " + searchResponse);

            // 6. 후보군 파싱
            List<JobRecommend> candidates = Arrays.stream(searchResponse.getHits().getHits())
                    .map(hit -> parseJobPosting(hit.getSourceAsMap()))
                    .toList();

            // 7. 회사 비율 적용 → 최종 추천 6개
            return distributeByCompanyRatio(candidates, companyRatio, 6);

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }



    @Async
    public void getFullRecommendationsAsync() {
    }

    @SuppressWarnings("unchecked")
    private JobRecommend parseJobPosting(Map<String, Object> source) {
        Map<String, Object> company = (Map<String, Object>) source.get("company");

        return JobRecommend.builder()
                .title((String) source.getOrDefault("title", ""))
                .companyName(company != null ? (String) company.getOrDefault("companyName", "") : "")
                .recruitmentAddress((String) source.getOrDefault("recruitmentAddress", ""))
                .ocrData((String) source.getOrDefault("ocrData", ""))
                .content((String) source.getOrDefault("content", ""))
                .companyType(company != null && company.get("companyType") != null
                        ? CompanyTypeEnum.valueOf(company.get("companyType").toString())
                        : null)
                .education(source.get("education") != null
                        ? Education.valueOf(source.get("education").toString())
                        : null)
                .recruitmentType(source.get("recruitmentType") != null
                        ? RecruitmentType.valueOf(source.get("recruitmentType").toString())
                        : null)
                .build();
    }


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

}
