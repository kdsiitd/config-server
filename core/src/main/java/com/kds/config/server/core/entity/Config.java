package com.kds.config.server.core.entity;

import com.kds.config.server.core.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Configuration entity representing a configuration property in the system.
 * 
 * @author KDS Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "config")
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Configuration property entity")
@Builder
public class Config extends BaseEntity {

    @Column(name = "application", length = 50, nullable = false)
    @Schema(description = "Application name", example = "user-service", required = true, maxLength = 50)
    private String application;

    @Column(name = "profile", length = 20, nullable = false)
    @Schema(description = "Environment profile", example = "prod", required = true, maxLength = 20)
    private String profile;

    @Column(name = "label", length = 100)
    @Schema(description = "Version label or branch", example = "v1.0.0", maxLength = 100)
    private String label;

    @Column(name = "prop_key", length = 100, nullable = false)
    @Schema(description = "Configuration property key", example = "database.url", required = true, maxLength = 100)
    private String propKey;

    @Column(name = "prop_value", length = 500, nullable = false, columnDefinition = "TEXT")
    @Schema(description = "Configuration property value", example = "jdbc:postgresql://prod-db:5432/userdb", required = true, maxLength = 500)
    private String propValue;
} 