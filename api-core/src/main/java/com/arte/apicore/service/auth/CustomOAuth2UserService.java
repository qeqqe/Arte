package com.arte.apicore.service.auth;

import com.arte.apicore.dto.GitHubEmail;
import com.arte.apicore.entity.Users;
import com.arte.apicore.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@Service
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;


    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String githubUsername = oauth2User.getAttribute("login");
        String accessToken = userRequest.getAccessToken().getTokenValue();
        String email = getPrimaryEmail(accessToken);

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

    private String getPrimaryEmail(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<GitHubEmail[]> response = restTemplate.exchange(
                    "https://api.github.com/user/emails",
                    HttpMethod.GET,
                    entity,
                    GitHubEmail[].class
            );

            GitHubEmail[] emails = response.getBody();
            if (emails == null || emails.length == 0) {
                return null;
            }

            // find primary
            return Arrays.stream(emails)
                    .filter(e -> e != null && e.primary())
                    .map(GitHubEmail::email)
                    .findFirst()
                    .orElseGet(() -> emails[0] != null ? emails[0].email() : null);

        } catch (Exception e) {
            log.warn("Couldn't find user's email: " + e);
            return null;
        }
    }

}