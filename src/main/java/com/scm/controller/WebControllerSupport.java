package com.scm.controller;

import com.scm.model.User;
import com.scm.util.DataStore;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;

/**
 * Shared helpers for web controllers.
 */
public abstract class WebControllerSupport {

    protected <T extends User> T getAuthenticatedUser(HttpSession session, Class<T> type) {
        Object raw = session.getAttribute("user");
        if (!(raw instanceof User sessionUser)) {
            return null;
        }
        User fresh = DataStore.findUserById(sessionUser.getUserId());
        if (fresh == null || !fresh.isActive()) {
            session.invalidate();
            return null;
        }
        session.setAttribute("user", fresh);
        if (!type.isInstance(fresh)) {
            return null;
        }
        return type.cast(fresh);
    }

    protected void flashSuccess(HttpSession session, String message) {
        session.setAttribute("flashSuccess", message);
    }

    protected void flashError(HttpSession session, String message) {
        session.setAttribute("flashError", message);
    }

    protected void applyFlash(HttpSession session, Model model) {
        Object success = session.getAttribute("flashSuccess");
        Object error = session.getAttribute("flashError");
        if (success != null) {
            model.addAttribute("success", success.toString());
            session.removeAttribute("flashSuccess");
        }
        if (error != null) {
            model.addAttribute("error", error.toString());
            session.removeAttribute("flashError");
        }
    }
}
