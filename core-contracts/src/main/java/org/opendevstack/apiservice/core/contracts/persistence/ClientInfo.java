package org.opendevstack.apiservice.core.contracts.persistence;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public final class ClientInfo {

    private final UUID id;
    private final String clientId;
    private final String name;
    private final boolean enabled;
}
