package com.example.userservice.config;

import com.example.userservice.service.UserService;
import com.example.userservice.jwt.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtils jwtUtils;
    private final UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // Get email and name from OAuth2 provider
        String email = oAuth2User.getAttribute("email");
        if (email == null) {
            // fallback for providers without email
            email = "user_" + oAuth2User.getAttribute("id") + "@example.com";
        }
        String name = oAuth2User.getAttribute("name");

        // Persist user in DB if not exists
        System.out.println("Registering OAuth2 user: " + email);
        userService.registerOAuthUser(email, name);

        // Generate JWT token
        String token = jwtUtils.generateToken(email);

        // ✅ Redirect to frontend SPA with token in query params
        String redirectUrl = "http://localhost:5173/auth?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
        response.sendRedirect(redirectUrl);
    }
}
