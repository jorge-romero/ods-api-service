package org.opendevstack.apiservice.core.contracts.api;

import java.util.List;

public interface ApiModule {

    String getName();

    String getBasePath();

    List<String> getSupportedVersions();

    boolean isLocal();
}
