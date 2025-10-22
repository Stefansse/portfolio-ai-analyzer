package com.example.userservice.config;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // ✅ Only responsible for fetching user info
        OAuth2User oAuth2User = super.loadUser(userRequest);

        System.out.println("🔹 CustomOAuth2UserService loadUser called for registrationId: "
                + userRequest.getClientRegistration().getRegistrationId());

        return oAuth2User;
    }
}
