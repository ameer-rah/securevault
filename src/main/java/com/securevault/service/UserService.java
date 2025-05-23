package com.securevault.service;

import com.securevault.model.User;
import com.securevault.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final EncryptionService encryptionService;
    private final PasswordEncoder passwordEncoder;

    // In-memory storage for master passwords during session
    private final ConcurrentMap<String, String> masterPasswordStore = new ConcurrentHashMap<>();

    @Autowired
    public UserService(UserRepository userRepository,
                       EncryptionService encryptionService,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.encryptionService = encryptionService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Register a new user
     */
    public User registerUser(String username, String email, String masterPassword) {
        // Check if username or email already exists
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Generate salt for password encryption
        byte[] salt = encryptionService.generateSalt();

        // Hash the master password
        String hashedPassword = passwordEncoder.encode(masterPassword);

        User user = new User(username, hashedPassword, salt, email);
        return userRepository.save(user);
    }

    /**
     * Authenticate a user
     */
    public boolean authenticateUser(String username, String masterPassword) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            boolean authenticated = passwordEncoder.matches(masterPassword, user.getPasswordHash());

            // Store master password for the session if authenticated
            if (authenticated) {
                masterPasswordStore.put(username, masterPassword);
            }

            return authenticated;
        }
        return false;
    }

    /**
     * Get the master password for the current session
     */
    public String getMasterPassword(String username) {
        String masterPassword = masterPasswordStore.get(username);
        if (masterPassword == null) {
            throw new SecurityException("User not authenticated or session expired");
        }
        return masterPassword;
    }

    /**
     * Logout user (remove master password from memory)
     */
    public void logout(String username) {
        masterPasswordStore.remove(username);
    }

    /**
     * Get current user
     */
    public User getCurrentUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    /**
     * Update user details
     */
    public User updateUser(String username, String email, String passwordHint) {
        User user = getCurrentUser(username);

        // Only update email if it's changed and not used by another user
        if (!email.equals(user.getEmail()) && userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already in use");
        }

        user.setEmail(email);
        user.setMasterPasswordHint(passwordHint);

        return userRepository.save(user);
    }

    /**
     * Change master password
     */
    public void changePassword(String username, String currentPassword, String newPassword) {
        User user = getCurrentUser(username);

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new SecurityException("Current password is incorrect");
        }

        // Update password hash
        String newPasswordHash = passwordEncoder.encode(newPassword);
        user.setPasswordHash(newPasswordHash);

        // Generate new salt
        byte[] newSalt = encryptionService.generateSalt();
        user.setSalt(newSalt);

        userRepository.save(user);

        // Update in-memory master password
        masterPasswordStore.put(username, newPassword);
    }
}