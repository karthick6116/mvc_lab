package com.scm.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Date;

/**
 * Simple user notification.
 */
@Entity
@Table(name = "notifications")
public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(length = 36)
    private String notificationId;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User recipient;
    
    @Column(nullable = false, length = 100)
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;
    
    @Column(length = 50)
    private String type;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "sent_date")
    private Date sentDate;
    
    @Column(name = "is_read")
    private boolean isRead;
    
    @Column(length = 20)
    private String priority;

    public Notification() {
    }

    public Notification(String notificationId, User recipient, String title,
                        String message, String type) {
        this.notificationId = notificationId;
        this.recipient = recipient;
        this.title = title;
        this.message = message;
        this.type = type;
        this.sentDate = new Date();
        this.isRead = false;
        this.priority = "NORMAL";
    }

    public void send() {
        // no-op in web app
    }

    public void markAsRead() {
        this.isRead = true;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public User getRecipient() {
        return recipient;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public Date getSentDate() {
        return sentDate;
    }

    public boolean isRead() {
        return isRead;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }
}
