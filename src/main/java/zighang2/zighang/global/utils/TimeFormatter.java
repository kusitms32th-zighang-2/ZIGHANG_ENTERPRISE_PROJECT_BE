package zighang2.zighang.global.utils;



import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class TimeFormatter {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    public String formatISOKST() {
        ZonedDateTime now = ZonedDateTime.now(KST);
        ZonedDateTime arrivalKst = ZonedDateTime.of(now.toLocalDate().plusDays(1), LocalTime.of(9, 0), KST);

        return arrivalKst.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ"));
    }

    public String formatDTTM(ZonedDateTime zonedDateTime) {
        return zonedDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
    }
}
