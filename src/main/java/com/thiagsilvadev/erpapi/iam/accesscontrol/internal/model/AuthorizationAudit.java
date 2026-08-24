package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model;

import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.thiagsilvadev.erpapi.common.domain.model.AbstractBaseEntity;
import com.thiagsilvadev.erpapi.common.domain.validation.Strings;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.exception.UserActorIdRequiredException;

@Entity
@Table(
        name = "iam_authorization_audits",
        indexes = {
            @Index(name = "idx_iam_authorization_audits_company_created", columnList = "company_id, created_at"),
            @Index(name = "idx_iam_authorization_audits_target", columnList = "target_type, target_id")
        })
public class AuthorizationAudit extends AbstractBaseEntity {

    @Column(name = "company_id", updatable = false)
    private UUID companyId;

    @Column(name = "actor_user_id", updatable = false)
    private UUID actorUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "actor_type", nullable = false, updatable = false, length = 20)
    private AuthorizationActorType actorType;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, updatable = false, length = 40)
    private AuthorizationTargetType targetType;

    @Column(name = "target_id", nullable = false, updatable = false)
    private UUID targetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 40)
    private AuthorizationAction action;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "before_value", updatable = false)
    private String beforeValue;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "after_value", updatable = false)
    private String afterValue;

    protected AuthorizationAudit() {}

    private AuthorizationAudit(
            UUID companyId,
            UUID actorUserId,
            AuthorizationActorType actorType,
            AuthorizationTargetType targetType,
            UUID targetId,
            AuthorizationAction action,
            String beforeValue,
            String afterValue) {
        this.companyId = companyId;
        this.actorUserId = actorUserId;
        this.actorType = actorType;
        this.targetType = targetType;
        this.targetId = targetId;
        this.action = action;
        this.beforeValue = Strings.trimToNull(beforeValue);
        this.afterValue = Strings.trimToNull(afterValue);
    }

    public static AuthorizationAudit create(
            UUID companyId,
            UUID actorUserId,
            AuthorizationActorType actorType,
            AuthorizationTargetType targetType,
            UUID targetId,
            AuthorizationAction action,
            String beforeValue,
            String afterValue) {
        Objects.requireNonNull(targetId, "Audit target id cannot be null");
        Objects.requireNonNull(targetType, "Audit target type cannot be null");
        Objects.requireNonNull(action, "Audit action cannot be null");
        Objects.requireNonNull(actorType, "Audit actor type cannot be null");
        if (actorType == AuthorizationActorType.USER && actorUserId == null) {
            throw new UserActorIdRequiredException();
        }
        return new AuthorizationAudit(
                companyId, actorUserId, actorType, targetType, targetId, action, beforeValue, afterValue);
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public UUID getActorUserId() {
        return actorUserId;
    }

    public AuthorizationActorType getActorType() {
        return actorType;
    }

    public AuthorizationTargetType getTargetType() {
        return targetType;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public AuthorizationAction getAction() {
        return action;
    }

    public String getBeforeValue() {
        return beforeValue;
    }

    public String getAfterValue() {
        return afterValue;
    }
}
