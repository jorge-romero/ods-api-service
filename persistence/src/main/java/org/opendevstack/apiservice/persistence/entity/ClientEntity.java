package org.opendevstack.apiservice.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
public class ClientEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String azureClientId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean enabled = true;
}
