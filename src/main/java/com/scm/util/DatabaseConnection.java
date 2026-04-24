package com.scm.util;

/**
 * Backward-compatible placeholder.
 * The web app now uses file-backed persistence through DataStore,
 * so no external database setup is required.
 */
public class DatabaseConnection {

    public static Object getConnection() {
        return null;
    }

    public static void closeConnection() {
        // no-op
    }

    public static boolean testConnection() {
        return true;
    }
}
