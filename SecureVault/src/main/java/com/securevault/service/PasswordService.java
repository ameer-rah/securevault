package com.securevault.service;

import com.securevault.model.PasswordEntry;
import com.securevault.model.User;
import com.securevault.repository.PasswordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PasswordService {

    private final PasswordRepository passwordRepository;
    private final EncryptionService encryptionService;
    private final UserService userService;

    @Autowired
    public PasswordService(PasswordRepository passwordRepository,
                           EncryptionService encryptionService,
                           UserService userService) {
        this.passwordRepository = passwordRepository;
        this.encryptionService = encryptionService;
        this.userService = userService;
    }

    /**
     * Get all passwords for a user
     */
    public List<PasswordEntry> getAllPasswords(String username) {
        User user = userService.getCurrentUser(username);
        return passwordRepository.findByUser(user);
    }

    /**
     * Get passwords by category
     */
    public List<PasswordEntry> getPasswordsByCategory(String username, int category) {
        User user = userService.getCurrentUser(username);
        return passwordRepository.findByUserAndCategory(user, category);
    }

    /**
     * Search passwords by website
     */
    public List<PasswordEntry> searchPasswords(String username, String search) {
        User user = userService.getCurrentUser(username);
        return passwordRepository.findByUserAndWebsiteContainingIgnoreCase(user, search);
    }

    /**
     * Save a new password entry
     */
    public PasswordEntry savePassword(String website, String username, String password,
                                      String notes, int category, String currentUsername) throws Exception {
        User user = userService.getCurrentUser(currentUsername);
        SecretKey key = encryptionService.deriveKey(userService.getMasterPassword(currentUsername), user.getSalt());
        String encryptedPassword = encryptionService.encryptPassword(password, key);

        PasswordEntry entry = new PasswordEntry(website, username, encryptedPassword, user);
        entry.setNotes(notes);
        entry.setCategory(category);

        // Make sure we return the saved entity
        return passwordRepository.save(entry);
    }

    /**
     * Update an existing password entry
     */
    public PasswordEntry updatePassword(Long id, String website, String username, String password,
                                        String notes, int category, String currentUsername) throws Exception {
        Optional<PasswordEntry> optionalEntry = passwordRepository.findById(id);
        if (optionalEntry.isEmpty()) {
            throw new IllegalArgumentException("Password entry not found");
        }

        PasswordEntry entry = optionalEntry.get();
        User user = userService.getCurrentUser(currentUsername);

        // Security check - ensure the entry belongs to the current user
        if (!entry.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Not authorized to update this password entry");
        }

        // Update fields
        entry.setWebsite(website);
        entry.setUsername(username);
        entry.setNotes(notes);
        entry.setCategory(category);

        // Only update password if a new one is provided
        if (password != null && !password.isEmpty()) {
            SecretKey key = encryptionService.deriveKey(userService.getMasterPassword(currentUsername), user.getSalt());
            String encryptedPassword = encryptionService.encryptPassword(password, key);
            entry.setEncryptedPassword(encryptedPassword);
        }

        entry.updateModificationTime();
        return passwordRepository.save(entry);
    }

    /**
     * Delete a password entry
     */
    public void deletePassword(Long id, String currentUsername) {
        Optional<PasswordEntry> optionalEntry = passwordRepository.findById(id);
        if (optionalEntry.isEmpty()) {
            throw new IllegalArgumentException("Password entry not found");
        }

        PasswordEntry entry = optionalEntry.get();
        User user = userService.getCurrentUser(currentUsername);

        // Security check - ensure the entry belongs to the current user
        if (!entry.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Not authorized to delete this password entry");
        }

        passwordRepository.delete(entry);
    }

    /**
     * Get a specific password entry
     */
    public PasswordEntry getPassword(Long id, String currentUsername) {
        Optional<PasswordEntry> optionalEntry = passwordRepository.findById(id);
        if (optionalEntry.isEmpty()) {
            throw new IllegalArgumentException("Password entry not found");
        }

        PasswordEntry entry = optionalEntry.get();
        User user = userService.getCurrentUser(currentUsername);

        // Security check - ensure the entry belongs to the current user
        if (!entry.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Not authorized to access this password entry");
        }

        // Update last accessed time
        entry.updateAccessTime();
        passwordRepository.save(entry);

        return entry;
    }

    /**
     * Get the decrypted password for an entry
     */
    public String getDecryptedPassword(Long id, String currentUsername) throws Exception {
        PasswordEntry entry = getPassword(id, currentUsername);
        User user = userService.getCurrentUser(currentUsername);

        SecretKey key = encryptionService.deriveKey(userService.getMasterPassword(currentUsername), user.getSalt());
        return encryptionService.decryptPassword(entry.getEncryptedPassword(), key);
    }
}