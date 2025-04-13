package com.kds.config.server.core.entity.base;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.sql.Timestamp;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    private Timestamp createdAt;

    @LastModifiedDate
    private Timestamp updatedAt;

    @CreatedBy
    private String createdBy;

    @LastModifiedBy
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        createdBy = createdBy == null ? "SYSTEM" : createdBy;
        updatedBy = updatedBy == null ? "SYSTEM" : updatedBy;
        createdAt = createdAt == null ? new Timestamp(System.currentTimeMillis()) : createdAt;
        updatedAt = updatedAt == null ? new Timestamp(System.currentTimeMillis()) : updatedAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedBy = updatedBy == null ? "SYSTEM" : updatedBy;
        updatedAt = updatedAt == null ? new Timestamp(System.currentTimeMillis()) : updatedAt;
    }

}