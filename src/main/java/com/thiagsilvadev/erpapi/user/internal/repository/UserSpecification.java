package com.thiagsilvadev.erpapi.user.internal.repository;

import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.thiagsilvadev.erpapi.common.model.Email;
import com.thiagsilvadev.erpapi.user.api.UserRole;
import com.thiagsilvadev.erpapi.user.api.UserStatus;
import com.thiagsilvadev.erpapi.user.internal.dto.UserSearchCriteria;
import com.thiagsilvadev.erpapi.user.internal.model.User;

public class UserSpecification {

    private UserSpecification() {}

    public static Specification<User> withCriteria(UserSearchCriteria criteria) {
        return Specification.where(hasEmail(criteria.email()))
                .and(hasNameLike(criteria.name()))
                .and(hasRole(criteria.role()))
                .and(hasStatus(criteria.status()))
                .and(hasCompany(criteria.companyId()));
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

    private static Specification<User> hasRole(UserRole role) {
        return (root, query, cb) -> role == null ? null : cb.equal(root.get("role"), role);
    }

    private static Specification<User> hasStatus(UserStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    private static Specification<User> hasCompany(UUID companyId) {
        return (root, query, cb) -> companyId == null ? null : cb.equal(root.get("companyId"), companyId);
    }

    public static String normalizeEmail(String email) {
        return Email.create(email).getValue();
    }
}
