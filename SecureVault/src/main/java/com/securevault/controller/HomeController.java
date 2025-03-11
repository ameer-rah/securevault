package com.securevault.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for handling homepage and static page requests
 */
@Controller
public class HomeController {

    /**
     * Display the home page
     */
    @GetMapping("/")
    public String home() {
        return "index";
    }

    /**
     * Display the login page
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * Display the registration page
     */
    @GetMapping("/register")
    public String register() {
        return "register";
    }

    /**
     * Display the terms of service page
     */
    @GetMapping("/terms")
    public String terms() {
        return "terms";
    }

    /**
     * Display the privacy policy page
     */
    @GetMapping("/privacy")
    public String privacy() {
        return "privacy";
    }

    /**
     * Display the forgot password page
     */
    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "forgot-password";
    }
}