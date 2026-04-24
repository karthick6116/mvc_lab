package com.scm.model;

import com.scm.enums.UserRole;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Date;

/**
 * Base class for all users.
 */
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_type")
public abstract class User implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(length = 36)
    protected String userId;
    
    @Column(unique = true, nullable = false, length = 100)
    protected String username;
    
    @Column(length = 100)
    protected String email;
    
    @Column(nullable = false, length = 255)
    protected String password;
    
    @Column(length = 100)
    protected String name;
    
    @Column(length = 20)
    protected String phoneNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    protected UserRole role;
    
    @Column(name = "is_active")
    protected boolean isActive;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date")
    protected Date createdDate;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_login")
    protected Date lastLogin;

    protected User() {
    }

    public User(String userId, String username, String email, String password,
                String name, String phoneNumber, UserRole role) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.password = password;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.isActive = true;
        this.createdDate = new Date();
        this.lastLogin = null;
    }

    public boolean login(String username, String password) {
        boolean ok = isActive && this.username.equals(username) && this.password.equals(password);
        if (ok) {
            this.lastLogin = new Date();
        }
        return ok;
    }

    public void logout() {
        // no-op for web app
    }

    public void updateProfile(String email, String name, String phoneNumber) {
        this.email = email;
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public boolean changePassword(String oldPassword, String newPassword) {
        if (this.password.equals(oldPassword)) {
            this.password = newPassword;
            return true;
        }
        return false;
    }

    public void resetPassword(String newPassword) {
        this.password = newPassword;
    }

    public abstract void getDashboard();

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public UserRole getRole() {
        return role;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", role=" + role +
                ", active=" + isActive +
                '}';
    }
}
