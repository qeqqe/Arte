package com.arte.apicore.service.user;

import com.arte.apicore.entity.Users;
import com.arte.apicore.exception.UserNotFoundException;
import com.arte.apicore.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<Users> findByGithubUsername(String githubUsername) {
        return userRepository.findByGithubUsername(githubUsername);
    }

    public Optional<Users> findById(UUID id) {
        return userRepository.findById(id);
    }

    public Users getCurrentUser(String githubUsername) {
        return userRepository.findByGithubUsername(githubUsername)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + githubUsername));
    }

    public Users createOrUpdateUser(String email, String githubUsername, String githubToken) {
        Optional<Users> existingUser = userRepository.findByGithubUsername(githubUsername);
        
        if (existingUser.isPresent()) {
            Users user = existingUser.get();
            user.setEmail(email);
            user.setGithubToken(githubToken);
            return userRepository.save(user);
        } else {
            Users newUser = new Users(email, githubUsername, githubToken);
            return userRepository.save(newUser);
        }
    }
}