package org.example.quizapp.service;

import org.example.quizapp.dto.AuthRequest;
import org.example.quizapp.dto.AuthResponse;
import org.example.quizapp.dto.RegisterRequest;
import org.example.quizapp.entity.User;
import org.example.quizapp.repository.UserRepository;
import org.example.quizapp.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private NotificationService notificationService;

    public AuthResponse register(RegisterRequest request) {
        logger.info("Registering new user: {}", request.getUsername());
        
        if (userRepository.existsByUsername(request.getUsername())) {
            logger.error("Registration failed: Username {} already exists", request.getUsername());
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        userRepository.save(user);
        logger.info("User {} registered successfully with role {}", user.getUsername(), user.getRole());

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

        // Send async notification
        notificationService.sendRegistrationNotification(user.getUsername());

        return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }

    public AuthResponse login(AuthRequest request) {
        logger.info("User login attempt: {}", request.getUsername());
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
            
            logger.info("User {} logged in successfully", user.getUsername());

            return new AuthResponse(token, user.getUsername(), user.getRole().name());
        } catch (Exception e) {
            logger.error("Login failed for user: {}", request.getUsername(), e);
            throw e;
        }
    }
}
