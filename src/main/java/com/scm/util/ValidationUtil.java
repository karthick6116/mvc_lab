package com.scm.util;

import java.util.regex.Pattern;

/**
 * ValidationUtil - Provides validation methods for user inputs
 */
public class ValidationUtil {
    
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    
    private static final Pattern PHONE_PATTERN = 
        Pattern.compile("^\\d{3}-\\d{3}-\\d{4}$");
    
    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * Validate phone number format (XXX-XXX-XXXX)
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        return PHONE_PATTERN.matcher(phone).matches();
    }
    
    /**
     * Validate password strength
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        return true;
    }
    
    /**
     * Validate marks range
     */
    public static boolean isValidMarks(float marks, float totalMarks) {
        return marks >= 0 && marks <= totalMarks;
    }
    
    /**
     * Validate course credits
     */
    public static boolean isValidCredits(int credits) {
        return credits > 0 && credits <= 6;
    }
    
    /**
     * Validate seats
     */
    public static boolean isValidSeats(int seats) {
        return seats > 0 && seats <= 100;
    }
    
    /**
     * Validate non-empty string
     */
    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }
    
    /**
     * Validate positive amount
     */
    public static boolean isValidAmount(float amount) {
        return amount > 0;
    }
}
