package com.arte.apicore.controller;

import com.arte.apicore.dto.TokenResponse;
import com.arte.apicore.entity.Users;
import com.arte.apicore.exception.InvalidTokenException;
import com.arte.apicore.service.auth.strategy.JwtTokenProvider;
import com.arte.apicore.service.user.UserService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtTokenProvider tokenProvider;
    private final UserService userService;

    public AuthController(JwtTokenProvider tokenProvider, UserService userService) {
        this.tokenProvider = tokenProvider;
        this.userService = userService;
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @RequestBody(required = false) Map<String, String> request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        String refreshToken = null;

        if (request != null && request.containsKey("refreshToken")) {
            refreshToken = request.get("refreshToken");
        } else {
            Cookie[] cookies = httpRequest.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("refreshToken".equals(cookie.getName())) {
                        refreshToken = cookie.getValue();
                        break;
                    }
                }
            }
        }

        if (refreshToken == null || !tokenProvider.isValidRefreshToken(refreshToken)) {
            throw new InvalidTokenException("Invalid or expired refresh token");
        }

        Claims claims = tokenProvider.validateToken(refreshToken);
        String userId = claims.getId();
        String username = claims.getSubject();
        String email = claims.get("email", String.class);

        String newAccessToken = tokenProvider.generateAccessToken(userId, username, email);
        String newRefreshToken = tokenProvider.generateRefreshToken(userId, username, email);

        Cookie accessTokenCookie = createCookie("accessToken", newAccessToken, 4 * 60 * 60);
        Cookie refreshTokenCookie = createCookie("refreshToken", newRefreshToken, 7 * 24 * 60 * 60);

        httpResponse.addCookie(accessTokenCookie);
        httpResponse.addCookie(refreshTokenCookie);

        return ResponseEntity.ok(new TokenResponse(
                newAccessToken,
                newRefreshToken,
                tokenProvider.getAccessTokenExpiration() / 1000
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String githubUsername = auth.getName();
        Users user = userService.getCurrentUser(githubUsername);

        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("email", user.getEmail());
        response.put("githubUsername", user.getGithubUsername());
        response.put("createdAt", user.getCreatedAt());

        return ResponseEntity.ok(response);
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
