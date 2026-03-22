package com.guildflow.backend.util;

import com.guildflow.backend.exception.ForbiddenException;
import com.guildflow.backend.model.User;
import com.guildflow.backend.model.enums.Role;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    /**
     * Throws ForbiddenException if user is null or has a null role.
     */
    public void validateUserState(User user) {
        if (user == null || user.getRole() == null) {
            throw new ForbiddenException("Access denied: Invalid user state");
        }
    }

    /**
     * Throws ForbiddenException unless the current user is ADMIN or their ID matches the owner's ID.
     * If owner is null, non-ADMIN users are always denied.
     */
    public void requireAdminOrOwner(User currentUser, User owner, String deniedMessage) {
        if (currentUser.getRole() != Role.ADMIN &&
                (owner == null || !owner.getId().equals(currentUser.getId()))) {
            throw new ForbiddenException(deniedMessage);
        }
    }

    public boolean isAdmin(User user) {
        return user != null && user.getRole() == Role.ADMIN;
    }
}
