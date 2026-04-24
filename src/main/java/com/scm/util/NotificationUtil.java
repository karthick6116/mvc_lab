package com.scm.util;

import com.scm.model.Notification;
import com.scm.model.User;

/**
 * NotificationUtil - Helper class for sending notifications
 */
public class NotificationUtil {
    
    /**
     * Send enrollment approval notification
     */
    public static void sendEnrollmentApprovalNotification(User student, String courseName) {
        Notification notification = new Notification(
            "NOT" + System.currentTimeMillis(),
            student,
            "Enrollment Approved",
            "Your enrollment in " + courseName + " has been approved!",
            "ENROLLMENT"
        );
        notification.send();
        DataStore.addNotification(notification);
    }
    
    /**
     * Send enrollment rejection notification
     */
    public static void sendEnrollmentRejectionNotification(User student, String courseName, String reason) {
        Notification notification = new Notification(
            "NOT" + System.currentTimeMillis(),
            student,
            "Enrollment Rejected",
            "Your enrollment in " + courseName + " has been rejected. Reason: " + reason,
            "ENROLLMENT"
        );
        notification.send();
        DataStore.addNotification(notification);
    }
    
    /**
     * Send grade published notification
     */
    public static void sendGradePublishedNotification(User student, String courseName, String grade) {
        Notification notification = new Notification(
            "NOT" + System.currentTimeMillis(),
            student,
            "Grade Published",
            "Your grade for " + courseName + " has been published: " + grade,
            "GRADE"
        );
        notification.send();
        DataStore.addNotification(notification);
    }
    
    /**
     * Send payment success notification
     */
    public static void sendPaymentSuccessNotification(User student, String courseName, float amount) {
        Notification notification = new Notification(
            "NOT" + System.currentTimeMillis(),
            student,
            "Payment Successful",
            String.format("Payment of $%.2f for %s completed successfully.", amount, courseName),
            "PAYMENT"
        );
        notification.send();
        DataStore.addNotification(notification);
    }
    
    /**
     * Send payment failure notification
     */
    public static void sendPaymentFailureNotification(User student, String courseName) {
        Notification notification = new Notification(
            "NOT" + System.currentTimeMillis(),
            student,
            "Payment Failed",
            "Payment for " + courseName + " failed. Please try again.",
            "PAYMENT"
        );
        notification.send();
        DataStore.addNotification(notification);
    }
    
    /**
     * Send low attendance warning
     */
    public static void sendLowAttendanceWarning(User student, String courseName, float percentage) {
        Notification notification = new Notification(
            "NOT" + System.currentTimeMillis(),
            student,
            "Low Attendance Warning",
            String.format("Your attendance in %s is %.1f%%. Minimum required is 75%%.", 
                        courseName, percentage),
            "ATTENDANCE"
        );
        notification.setPriority("HIGH");
        notification.send();
        DataStore.addNotification(notification);
    }
    
    /**
     * Send course material uploaded notification
     */
    public static void sendMaterialUploadedNotification(User student, String courseName, String materialTitle) {
        Notification notification = new Notification(
            "NOT" + System.currentTimeMillis(),
            student,
            "New Course Material",
            "New material uploaded for " + courseName + ": " + materialTitle,
            "MATERIAL"
        );
        notification.send();
        DataStore.addNotification(notification);
    }
    
    /**
     * Send general notification
     */
    public static void sendGeneralNotification(User recipient, String title, String message) {
        Notification notification = new Notification(
            "NOT" + System.currentTimeMillis(),
            recipient,
            title,
            message,
            "GENERAL"
        );
        notification.send();
        DataStore.addNotification(notification);
    }
}
