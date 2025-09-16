package zighang2.zighang.global.utils;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class WorkExperienceFormatter {
    public String formatWorkExperience(String workExpRaw) {
        if (workExpRaw == null || workExpRaw.isBlank()) {
            return null;
        }

        List<Integer> values = Arrays.stream(workExpRaw.split("[,/]"))
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
