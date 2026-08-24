package com.thiagsilvadev.erpapi.user.internal.repository;

import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.thiagsilvadev.erpapi.common.domain.model.Email;
import com.thiagsilvadev.erpapi.user.api.UserStatus;
import com.thiagsilvadev.erpapi.user.internal.dto.UserSearchCriteria;
import com.thiagsilvadev.erpapi.user.internal.model.User;

public class UserSpecification {

    private UserSpecification() {}

    public static Specification<User> withCriteria(UserSearchCriteria criteria, Set<UUID> allowedUserIds) {
        return Specification.where(hasEmail(criteria.email()))
                .and(hasNameLike(criteria.name()))
                .and(hasStatus(criteria.status()))
                .and(hasIdIn(allowedUserIds));
    }

    private static Specification<User> hasEmail(String email) {
        return (root, query, cb) -> {
            if (email == null || email.isBlank()) {
                return null;
            }
            return cb.equal(root.get("contact").get("email").get("value"), normalizeEmail(email));
        };
    }

    private static Specification<User> hasNameLike(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) {
                return null;
            }
            String pattern = "%" + name.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("firstName")), pattern),
                    cb.like(cb.lower(root.get("lastName")), pattern));
        };
    }

    private static Specification<User> hasStatus(UserStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    private static Specification<User> hasIdIn(Set<UUID> ids) {
        return (root, query, cb) -> root.get("id").in(ids);
    }

    public static String normalizeEmail(String email) {
        return Email.create(email).getValue();
    }
}
