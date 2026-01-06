package com.arte.apicore.service.auth.strategy;

import com.arte.apicore.entity.Users;
import com.arte.apicore.service.user.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider tokenProvider;
    private final UserService userService;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final String frontendUrl;

    public OAuth2LoginSuccessHandler(
            JwtTokenProvider tokenProvider,
            UserService userService,
            OAuth2AuthorizedClientService authorizedClientService,
            @Value("${app.frontend-url:http://localhost:3000}") String frontendUrl) {
        this.tokenProvider = tokenProvider;
        this.userService = userService;
        this.authorizedClientService = authorizedClientService;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String githubUsername = oauth2User.getAttribute("login");
        String email = oauth2User.getAttribute("email");
        
        // If email is null, use a default email format
        if (email == null || email.isEmpty()) {
            email = githubUsername + "@github.user";
        }
        
        // Get the actual GitHub access token from authorized client
        String githubAccessToken = "default-token";
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
            OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                oauth2Token.getAuthorizedClientRegistrationId(),
                oauth2Token.getName()
            );
            if (authorizedClient != null && authorizedClient.getAccessToken() != null) {
                githubAccessToken = authorizedClient.getAccessToken().getTokenValue();
            }
        }
        
        // Create or update user in database
        Users user = userService.createOrUpdateUser(email, githubUsername, githubAccessToken);

        String userId = String.valueOf(user.getId());
        String accessToken = tokenProvider.generateAccessToken(userId, user.getGithubUsername(), user.getEmail());
        String refreshToken = tokenProvider.generateRefreshToken(userId, user.getGithubUsername(), user.getEmail());

        Cookie accessTokenCookie = createCookie("accessToken", accessToken, 4 * 60 * 60);
        Cookie refreshTokenCookie = createCookie("refreshToken", refreshToken, 7 * 24 * 60 * 60);

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        response.sendRedirect(frontendUrl + "/oauth/callback");
    }

    private Cookie createCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        cookie.setDomain("localhost");
        return cookie;
    }
}
