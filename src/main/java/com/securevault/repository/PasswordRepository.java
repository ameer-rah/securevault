package com.securevault.repository;

import com.securevault.model.PasswordEntry;
import com.securevault.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PasswordRepository extends JpaRepository<PasswordEntry, Long> {

    List<PasswordEntry> findByUser(User user);

    List<PasswordEntry> findByUserAndWebsiteContainingIgnoreCase(User user, String website);

    List<PasswordEntry> findByUserAndCategory(User user, int category);
}