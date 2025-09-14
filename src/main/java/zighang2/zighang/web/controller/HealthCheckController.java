package zighang2.zighang.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health-check")
@RequiredArgsConstructor
public class HealthCheckController {

    @GetMapping
    @Operation(summary = "서버 연결 확인 API", description = "서버 연결 확인 API입니다.(프론트 연결X)")
    public String healthCheck() {
        return "Server is running!";
    }
}
