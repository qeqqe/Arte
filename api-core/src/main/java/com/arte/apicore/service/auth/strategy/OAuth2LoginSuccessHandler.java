package com.arte.apicore.service.auth.strategy;

import com.arte.apicore.entity.Users;
import com.arte.apicore.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider tokenProvider;

    private final UserService userService;

    public OAuth2LoginSuccessHandler(JwtTokenProvider tokenProvider, UserService userService) {
        this.tokenProvider = tokenProvider;
        this.userService = userService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

        String githubUsername = oauth2User.getAttribute("login");

        Users user = userService.getCurrentUser(githubUsername);

        String jwtToken = tokenProvider.generateToken(user.getGithubUsername(), user.getEmail());

        response.sendRedirect("http://localhost:3000/oauth/callback?token=" + jwtToken);
    }
}
