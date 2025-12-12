package com.arte.apicore.service.auth;

import com.arte.apicore.entity.Users;
import com.arte.apicore.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String githubUsername = oauth2User.getAttribute("login");
        String email = oauth2User.getAttribute("email");
        String accessToken = userRequest.getAccessToken().getTokenValue();

        if (email == null || email.isEmpty()) {
            email = githubUsername + "@users.noreply.github.com";
        }

        String finalEmail = email;
        Users user = userRepository.findByGithubUsername(githubUsername)
                .map(existingUser -> {
                    existingUser.setEmail(finalEmail);
                    existingUser.setGithubToken(accessToken);
                    return userRepository.save(existingUser);
                })
                .orElseGet(() -> {
                    Users newUser = new Users(finalEmail, githubUsername, accessToken);
                    return userRepository.save(newUser);
                });

        return oauth2User;
    }
}