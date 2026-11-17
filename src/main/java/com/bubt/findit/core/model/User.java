package com.bubt.findit.core.model;

import java.time.LocalDateTime;

/**
 * The FindIt user record. Owned by Segment 1 and shared by every segment.
 *
 * <p>Items (Segment 2), matches and claims (Segment 3/4) all point at
 * {@code users.user_id}, so this class is the single source of truth for
 * "who did what".
 */
public class User {

    private Long id;
    private String fullName;
    private String email;
    private String studentId;
    private String passwordHash;
    private String passwordSalt;
    private Role role = Role.STUDENT;
    private UserStatus status = UserStatus.ACTIVE;
    private String phone;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;

    public User() {
    }

    public static User create(String fullName, String email, String studentId,
                              String passwordHash, String passwordSalt, Role role) {
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setStudentId(studentId);
        user.setPasswordHash(passwordHash);
        user.setPasswordSalt(passwordSalt);
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    /** Copy without the password material - safe to hand to the UI. */
    public User sanitized() {
        User copy = new User();
        copy.id = id;
        copy.fullName = fullName;
        copy.email = email;
        copy.studentId = studentId;
        copy.passwordHash = null;
        copy.passwordSalt = null;
        copy.role = role;
        copy.status = status;
        copy.phone = phone;
        copy.createdAt = createdAt;
        copy.lastLogin = lastLogin;
        return copy;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPasswordSalt() {
        return passwordSalt;
    }

    public void setPasswordSalt(String passwordSalt) {
        this.passwordSalt = passwordSalt;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public String initials() {
        if (fullName == null || fullName.isBlank()) {
            return "?";
        }
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase();
        }
        return (parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", email='" + email + "', role=" + role + '}';
    }
}
