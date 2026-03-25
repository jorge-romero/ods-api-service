package org.opendevstack.apiservice.persistence.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "api_definitions")
@Getter
@Setter
@NoArgsConstructor
public class ApiDefinitionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String apiId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String basePath;

    @Column(nullable = false)
    private String version;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "auth_types", columnDefinition = "varchar(30)[]", nullable = false)
    private String[] authTypes = new String[0];

    @Column(nullable = false)
    private boolean isPublic;

    private String proxyUrl;

    @Column(nullable = false)
    private boolean enabled = true;
}
