package zighang2.zighang.global.auth.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import zighang2.zighang.global.payload.exception.GeneralException;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ExceptionFilter extends OncePerRequestFilter {
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (GeneralException e) {
            setResponse(response,e.getErrorReasonHttpStatus().getHttpStatus().value(),e.getErrorReasonHttpStatus().getCode(),e.getErrorReasonHttpStatus().getMessage());
        }
    }

    private void setResponse(HttpServletResponse response, int status, String code, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(status);

        String json = objectMapper.writeValueAsString(Map.of(
                "isSuccess", false,
                "code", code,
                "message", message
        ));

        response.getWriter().write(json);
    }
}
