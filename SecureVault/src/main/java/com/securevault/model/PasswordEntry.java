package com.securevault.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_entries")
public class PasswordEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String website;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String encryptedPassword;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime lastModified;

    @Column
    private LocalDateTime lastAccessed;

    @Column
    private int category;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Needed for JPA
    public PasswordEntry() {
        this.createdAt = LocalDateTime.now();
    }

    public PasswordEntry(String website, String username, String encryptedPassword, User user) {
        this.website = website;
        this.username = username;
        this.encryptedPassword = encryptedPassword;
        this.user = user;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEncryptedPassword() { return encryptedPassword; }
    public void setEncryptedPassword(String encryptedPassword) { this.encryptedPassword = encryptedPassword; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastModified() { return lastModified; }
    public void setLastModified(LocalDateTime lastModified) { this.lastModified = lastModified; }

    public LocalDateTime getLastAccessed() { return lastAccessed; }
    public void setLastAccessed(LocalDateTime lastAccessed) { this.lastAccessed = lastAccessed; }

    public int getCategory() { return category; }
    public void setCategory(int category) { this.category = category; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    // Helper method to update access time
    public void updateAccessTime() {
        this.lastAccessed = LocalDateTime.now();
    }

    // Helper method to update modification time
    public void updateModificationTime() {
        this.lastModified = LocalDateTime.now();
    }
}