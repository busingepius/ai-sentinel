package io.aisentinel.core.enforcement;

import io.aisentinel.core.policy.EnforcementAction;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Applies an enforcement action to the request/response.
 * Returns true if the request should proceed (doFilter), false if response was already written.
 */
public interface EnforcementHandler {

    boolean apply(EnforcementAction action, HttpServletRequest request, HttpServletResponse response,
                  String identityHash, String endpoint);

    default boolean isQuarantined(String identityHash) {
        return false;
    }
}
