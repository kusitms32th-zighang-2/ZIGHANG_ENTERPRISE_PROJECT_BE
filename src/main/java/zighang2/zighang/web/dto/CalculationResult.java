package zighang2.zighang.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import zighang2.zighang.web.domain.enums.CompanyTypeEnum;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class CalculationResult {
    public List<CompanyTypeEnum> companyAnswers;
    public List<String> welfareAnswers;
    public Map<CompanyTypeEnum, Double> companyRatio;
    public String welfareFinal;
    public String companyTypeDisplay;

}
