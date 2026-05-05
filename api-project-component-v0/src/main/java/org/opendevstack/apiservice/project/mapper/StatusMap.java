package org.opendevstack.apiservice.project.mapper;

import org.opendevstack.apiservice.project.model.ComponentsStatusDTO;

import java.util.HashMap;
import java.util.Map;

public class StatusMap {

    private StatusMap() {
    }

    static final Map<String, ComponentsStatusDTO> STATUS_MAP = new HashMap<>();

    static {
        STATUS_MAP.put("CREATING", ComponentsStatusDTO.RUNNING);
        STATUS_MAP.put("CREATED", ComponentsStatusDTO.READY);
        STATUS_MAP.put("FAILED", ComponentsStatusDTO.FAILED);
        STATUS_MAP.put("DELETING", ComponentsStatusDTO.DELETING);
        STATUS_MAP.put("UNKNOWN", ComponentsStatusDTO.UNKNOWN);
    }

    static ComponentsStatusDTO toOldStatus(String status) {
        return STATUS_MAP.get(status);
    }
}
