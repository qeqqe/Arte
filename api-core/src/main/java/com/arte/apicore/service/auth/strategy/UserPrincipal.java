package com.arte.apicore.service.auth.strategy;


public record UserPrincipal(String userId, String username, String email) {}