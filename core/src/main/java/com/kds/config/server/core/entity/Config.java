package com.kds.config.server.core.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.Builder;

import com.kds.config.server.core.entity.base.BaseEntity;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "config")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class Config extends BaseEntity {

    @Column(name = "application", length = 50, nullable = false)
    private String application;

    @Column(name = "profile", length = 20, nullable = false)
    private String profile;

    @Column(name = "label", length = 100)
    private String label;

    @Column(name = "prop_key", length = 100, nullable = false)
    private String propKey;

    @Column(name = "prop_value", length = 500, nullable = false, columnDefinition = "TEXT")
    private String propValue;
} 