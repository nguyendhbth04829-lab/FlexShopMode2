package com.example.demo.security;

import com.example.demo.dto.response.PhanHoiApi;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        String uri = request.getRequestURI();
        String accept = request.getHeader("Accept");

        if (uri.startsWith("/api/") || (accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE))) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            PhanHoiApi<Void> apiResponse = PhanHoiApi.thatBai("Bạn chưa đăng nhập hoặc phiên làm việc đã hết hạn: " + authException.getMessage());
            response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
        } else {
            response.sendRedirect("/login?error=unauthorized");
        }
    }
}
