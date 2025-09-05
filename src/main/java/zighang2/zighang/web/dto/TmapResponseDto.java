package zighang2.zighang.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

public class TmapResponseDto {

    //자동차 경로 안내 응답
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public class TmapRouteResDto {
        private List<Feature> features;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Feature {
            private Properties properties;
        }
        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Properties {
            private int totalTime;
            private int totalDistance;
            private int totalFare;
        }
    }

    // 대중교통 경로 안내 응답 DTO
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public class TmapTransitResDto {
        private Plan plan;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Plan {
            private List<Itinerary> itineraries;
        }

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Itinerary {
            private int totalTime;
            private int totalDistance;
            private int transferCount;
        }
    }
}
