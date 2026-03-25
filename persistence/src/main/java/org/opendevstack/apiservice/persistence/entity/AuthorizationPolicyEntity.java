package org.opendevstack.apiservice.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "authorization_policies")
@Getter
@Setter
@NoArgsConstructor
public class AuthorizationPolicyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String apiDefinitionId;

    private String clientId;

    @Column(nullable = false)
    private String policyType;

    @Column(columnDefinition = "jsonb")
    private String policyConfig;
}
