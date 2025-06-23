package com.example.serviceregistration.service;

import com.example.serviceregistration.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserServiceClient {
    private final RestTemplate restTemplate;
    private final String serviceDbApiUrl;

    public UserServiceClient(RestTemplate restTemplate, @Value("${servicedb.api.url}") String serviceDbApiUrl) {
        this.restTemplate = restTemplate;
        this.serviceDbApiUrl = serviceDbApiUrl;
    }

    public List<UserDTO> getAllUsers() {
        log.info("Fetching all users from servicedb");
        UserDTO[] users = restTemplate.getForObject(serviceDbApiUrl + "/users", UserDTO[].class);
        return Arrays.asList(users != null ? users : new UserDTO[0]);
    }

    public Optional<UserDTO> getUserById(Long id) {
        log.info("Fetching user with ID: {}", id);
        try {
            UserDTO user = restTemplate.getForObject(serviceDbApiUrl + "/users/" + id, UserDTO.class);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            log.error("Failed to fetch user with ID {}: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<UserDTO> getUserByEmail(String email) {
        log.info("Fetching user with email: {}", email);
        List<UserDTO> users = getAllUsers();
        return users.stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst();
    }

    public UserDTO createUser(UserDTO userDTO) {
        log.info("Creating user with email: {}", userDTO.getEmail());
        return restTemplate.postForObject(serviceDbApiUrl + "/users", userDTO, UserDTO.class);
    }

    public UserDTO updateUser(Long id, UserDTO userDTO) {
        log.info("Updating user with ID: {}", id);
        restTemplate.put(serviceDbApiUrl + "/users/" + id, userDTO);
        return getUserById(id).orElseThrow(() -> {
            log.error("User with ID {} not found after update", id);
            return new RuntimeException("User not found with id " + id);
        });
    }

    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);
        restTemplate.delete(serviceDbApiUrl + "/users/" + id);
    }
}