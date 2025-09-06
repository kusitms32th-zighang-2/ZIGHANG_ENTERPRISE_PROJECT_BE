package zighang2.zighang.web.dto.tmap.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

public class TmapResponseDto {

    //자동차 경로 안내 응답
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TmapRouteResDto {
        @JsonProperty("features")
        private List<Feature> features;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Feature {
            @JsonProperty("type")
            private String type; // Feature

            @JsonProperty("geometry")
            private Geometry geometry;

            @JsonProperty("properties")
            private Properties properties;
        }

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Geometry {
            @JsonProperty("type")
            private String type; // Point

            @JsonProperty("coordinates")
            private Object coordinates;
        }

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Properties {
            @JsonProperty("totalTime")
            private Integer totalTime;
            @JsonProperty("totalDistance")
            private Integer totalDistance;
        }
    }

    // 대중교통 경로 안내 응답 DTO
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public class TmapTransitResDto {
        @JsonProperty("plan")
        private Plan plan;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Plan {
            @JsonProperty("itineraries")
            private List<Itinerary> itineraries;
        }

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Itinerary {
            @JsonProperty("totalTime")
            private int totalTime;
            @JsonProperty("totalDistance")
            private int totalDistance;
            @JsonProperty("transferCount")
            private int transferCount;
        }
    }
}
