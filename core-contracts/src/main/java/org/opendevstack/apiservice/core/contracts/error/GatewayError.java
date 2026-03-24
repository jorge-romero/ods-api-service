package org.opendevstack.apiservice.core.contracts.error;

import lombok.Value;

@Value
public class GatewayError {

    String code;
    String message;
}
