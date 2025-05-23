package com.securevault.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private byte[] salt;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String masterPasswordHint;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PasswordEntry> passwordEntries = new ArrayList<>();

    // Needed for JPA
    public User() {
    }

    public User(String username, String passwordHash, byte[] salt, String email) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.email = email;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public byte[] getSalt() { return salt; }
    public void setSalt(byte[] salt) { this.salt = salt; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMasterPasswordHint() { return masterPasswordHint; }
    public void setMasterPasswordHint(String masterPasswordHint) { this.masterPasswordHint = masterPasswordHint; }

    public List<PasswordEntry> getPasswordEntries() { return passwordEntries; }
    public void setPasswordEntries(List<PasswordEntry> passwordEntries) { this.passwordEntries = passwordEntries; }

    // Helper method to add a password entry
    public void addPasswordEntry(PasswordEntry passwordEntry) {
        passwordEntries.add(passwordEntry);
        passwordEntry.setUser(this);
    }

    // Helper method to remove a password entry
    public void removePasswordEntry(PasswordEntry passwordEntry) {
        passwordEntries.remove(passwordEntry);
        passwordEntry.setUser(null);
    }
}