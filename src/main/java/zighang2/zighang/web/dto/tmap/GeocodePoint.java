package zighang2.zighang.web.dto.tmap;

import lombok.Getter;

@Getter
public class GeocodePoint {
    private final double latitude;
    private final double longitude;

    public GeocodePoint(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String latAsString() {
        return String.valueOf(latitude);
    }

    public String lonAsString() {
        return String.valueOf(longitude);
    }
}
