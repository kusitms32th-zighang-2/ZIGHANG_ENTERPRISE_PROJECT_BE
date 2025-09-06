package zighang2.zighang.web.dto.tmap.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TmapGeocodingResponseDto {

    @JsonProperty("coordinateInfo")
    private CoordinateInfo coordinateInfo;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CoordinateInfo {
        @JsonProperty("coordinate")
        private List<Coordinate> coordinate;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Coordinate {
        @JsonProperty("newLat")
        private String lat;
        @JsonProperty("newLon")
        private String lon;
        @JsonProperty("newLatEntr")
        private String latEntr;
        @JsonProperty("newlLonEntr")
        private String lonEntr;

    }
}
