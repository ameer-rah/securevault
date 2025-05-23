package com.securevault.controller;

import com.securevault.model.User;
import com.securevault.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controller for handling user authentication and account management
 */
@Controller
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Process registration form submission
     */
    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                               @RequestParam String email,
                               @RequestParam String password,
                               @RequestParam String confirmPassword,
                               @RequestParam(required = false) String passwordHint,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        // Validate password confirmation
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match");
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            model.addAttribute("passwordHint", passwordHint);
            return "register";
        }

        // Validate password strength
        if (password.length() < 8) {
            model.addAttribute("error", "Password must be at least 8 characters long");
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            model.addAttribute("passwordHint", passwordHint);
            return "register";
        }

        try {
            // Register the user
            userService.registerUser(username, email, password);

            // Update password hint if provided
            if (passwordHint != null && !passwordHint.trim().isEmpty()) {
                userService.updateUser(username, email, passwordHint);
            }

            redirectAttributes.addFlashAttribute("success", "Registration successful! Please log in.");
            return "redirect:/login";

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            model.addAttribute("passwordHint", passwordHint);
            return "register";
        }
    }

    /**
     * Process logout request
     */
    @GetMapping("/logout")
    public String logoutPage(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            String username = auth.getName();
            userService.logout(username);
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:/login?logout";
    }

    /**
     * Display user settings page
     */
    @GetMapping("/settings")
    public String settings(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            User user = userService.getCurrentUser(auth.getName());
            model.addAttribute("user", user);
        }
        return "settings";
    }

    /**
     * Process settings update form submission
     */
    @PostMapping("/settings/update")
    public String updateSettings(@RequestParam String email,
                                 @RequestParam(required = false) String passwordHint,
                                 RedirectAttributes redirectAttributes) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            try {
                userService.updateUser(auth.getName(), email, passwordHint);
                redirectAttributes.addFlashAttribute("success", "Settings updated successfully!");
            } catch (IllegalArgumentException e) {
                redirectAttributes.addFlashAttribute("error", e.getMessage());
            }
        }

        return "redirect:/settings";
    }

    /**
     * Process password change form submission
     */
    @PostMapping("/settings/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 RedirectAttributes redirectAttributes) {

        // Validate new password confirmation
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("passwordError", "New passwords do not match");
            return "redirect:/settings";
        }

        // Validate new password strength
        if (newPassword.length() < 8) {
            redirectAttributes.addFlashAttribute("passwordError", "New password must be at least 8 characters long");
            return "redirect:/settings";
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            try {
                userService.changePassword(auth.getName(), currentPassword, newPassword);
                redirectAttributes.addFlashAttribute("passwordSuccess", "Password changed successfully!");
            } catch (SecurityException e) {
                redirectAttributes.addFlashAttribute("passwordError", e.getMessage());
            }
        }

        return "redirect:/settings";
    }
}