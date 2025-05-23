package com.securevault.controller;

import com.securevault.model.PasswordEntry;

import com.securevault.service.PasswordGeneratorService;
import com.securevault.service.PasswordService;
import com.securevault.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for handling password management operations
 */
@Controller
public class PasswordController {
    private static final Logger logger = LoggerFactory.getLogger(PasswordController.class);

    private final PasswordService passwordService;
    private final UserService userService;
    private final PasswordGeneratorService passwordGeneratorService;

    @Autowired
    public PasswordController(PasswordService passwordService,
                              UserService userService,
                              PasswordGeneratorService passwordGeneratorService) {
        this.passwordService = passwordService;
        this.userService = userService;
        this.passwordGeneratorService = passwordGeneratorService;
    }

    /**
     * Display the dashboard with user's passwords
     */
    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) Integer category,
                            @RequestParam(required = false) String search,
                            Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            logger.warn("Unauthenticated access attempt to dashboard");
            return "redirect:/login";
        }

        String username = auth.getName();
        List<PasswordEntry> passwords;

        try {
            // Get passwords based on filter criteria
            if (category != null) {
                passwords = passwordService.getPasswordsByCategory(username, category);
            } else if (search != null && !search.trim().isEmpty()) {
                passwords = passwordService.searchPasswords(username, search);
            } else {
                passwords = passwordService.getAllPasswords(username);
            }

            model.addAttribute("passwords", passwords);
        } catch (Exception e) {
            logger.error("Error retrieving passwords for user {}: {}", username, e.getMessage(), e);
            model.addAttribute("error", "Error retrieving passwords: " + e.getMessage());
            model.addAttribute("passwords", Collections.emptyList());
        }

        return "dashboard";
    }

    /**
     * Save or update a password entry
     */
    @PostMapping("/password/save")
    public String savePassword(@RequestParam(required = false) Long id,
                               @RequestParam String website,
                               @RequestParam String username,
                               @RequestParam String password,
                               @RequestParam int category,
                               @RequestParam(required = false) String notes,
                               RedirectAttributes redirectAttributes) {

        logger.info("Received save request for website: {}, category: {}, id: {}",
                website, category, (id != null ? id : "new record"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            logger.warn("Unauthenticated save password attempt");
            redirectAttributes.addFlashAttribute("error", "You must be logged in to save passwords");
            return "redirect:/login";
        }

        String currentUsername = auth.getName();

        try {
            if (id != null) {
                // Update existing password
                PasswordEntry updated = passwordService.updatePassword(id, website, username, password, notes, category, currentUsername);
                logger.info("Password updated successfully for website: {}, ID: {}", website, updated.getId());
                redirectAttributes.addFlashAttribute("success", "RECORD UPDATED SUCCESSFULLY");
            } else {
                // Create new password
                PasswordEntry created = passwordService.savePassword(website, username, password, notes, category, currentUsername);
                logger.info("Password created successfully for website: {}, ID: {}", website, created.getId());
                redirectAttributes.addFlashAttribute("success", "NEW RECORD CREATED SUCCESSFULLY");
            }
        } catch (Exception e) {
            logger.error("Error saving password for user {}: {}", currentUsername, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "ERROR: " + e.getMessage());
        }

        return "redirect:/dashboard";
    }

    /**
     * Delete a password entry
     */
    @PostMapping("/password/delete")
    public String deletePassword(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            logger.warn("Unauthenticated delete password attempt");
            redirectAttributes.addFlashAttribute("error", "You must be logged in to delete passwords");
            return "redirect:/login";
        }

        String currentUsername = auth.getName();

        try {
            passwordService.deletePassword(id, currentUsername);
            logger.info("Password deleted successfully, ID: {}, User: {}", id, currentUsername);
            redirectAttributes.addFlashAttribute("success", "RECORD DELETED SUCCESSFULLY");
        } catch (Exception e) {
            logger.error("Error deleting password for user {}: {}", currentUsername, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", redirectAttributes.addFlashAttribute("error", "ERROR: " + e.getMessage()));
        }

        return "redirect:/dashboard";
    }

    /**
     * Get password details for AJAX requests
     */
    @GetMapping("/password/{id}")
    @ResponseBody
    public ResponseEntity<?> getPassword(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            logger.warn("Unauthenticated get password attempt");
            return ResponseEntity.badRequest().body(Map.of("error", "Not authenticated"));
        }

        String currentUsername = auth.getName();

        try {
            PasswordEntry entry = passwordService.getPassword(id, currentUsername);
            String decryptedPassword = passwordService.getDecryptedPassword(id, currentUsername);

            Map<String, Object> response = new HashMap<>();
            response.put("id", entry.getId());
            response.put("website", entry.getWebsite());
            response.put("username", entry.getUsername());
            response.put("password", decryptedPassword);
            response.put("category", entry.getCategory());
            response.put("notes", entry.getNotes());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving password for user {}: {}", currentUsername, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Generate a password
     */
    @GetMapping("/password/generate")
    @ResponseBody
    public ResponseEntity<?> generatePassword(@RequestParam(defaultValue = "16") int length,
                                              @RequestParam(defaultValue = "true") boolean lowercase,
                                              @RequestParam(defaultValue = "true") boolean uppercase,
                                              @RequestParam(defaultValue = "true") boolean digits,
                                              @RequestParam(defaultValue = "true") boolean special) {

        try {
            String generatedPassword = passwordGeneratorService.generatePassword(
                    length, lowercase, uppercase, digits, special);
            double strength = passwordGeneratorService.calculatePasswordStrength(generatedPassword);
            String strengthCategory = passwordGeneratorService.getPasswordStrengthCategory(generatedPassword);

            Map<String, Object> response = new HashMap<>();
            response.put("password", generatedPassword);
            response.put("strength", strength);
            response.put("strengthCategory", strengthCategory);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error generating password: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Test save method for debugging purposes
     */
    @GetMapping("/password/test-save")
    @ResponseBody
    public String testSave() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) {
                logger.warn("No authenticated user found for test save");
                return "No authenticated user found";
            }

            String username = auth.getName();
            userService.getCurrentUser(username);

            // Directly use values instead of calling getters on a new object
            PasswordEntry saved = passwordService.savePassword(
                    "test.com",       // website
                    "testuser",       // username
                    "testPassword123", // password
                    null,             // notes
                    1,                // category
                    username          // current username
            );

            logger.info("Test record saved successfully with ID: {}", saved.getId());
            return "Test record saved successfully with ID: " + saved.getId();
        } catch (Exception e) {
            logger.error("Error saving test record: {}", e.getMessage(), e);
            return "Error saving test record: " + e.getMessage();
        }
    }
}