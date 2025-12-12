package com.arte.apicore.service.auth.strategy;

import com.arte.apicore.entity.Users;
import com.arte.apicore.service.user.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider tokenProvider;
    private final UserService userService;
    private final String frontendUrl;

    public OAuth2LoginSuccessHandler(
            JwtTokenProvider tokenProvider,
            UserService userService,
            @Value("${app.frontend-url:http://localhost:3000}") String frontendUrl) {
        this.tokenProvider = tokenProvider;
        this.userService = userService;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String githubUsername = oauth2User.getAttribute("login");
        Users user = userService.getCurrentUser(githubUsername);

        String accessToken = tokenProvider.generateAccessToken(user.getGithubUsername(), user.getEmail());
        String refreshToken = tokenProvider.generateRefreshToken(user.getGithubUsername(), user.getEmail());

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
