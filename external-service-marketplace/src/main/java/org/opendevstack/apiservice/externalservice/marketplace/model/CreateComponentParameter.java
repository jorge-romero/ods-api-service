package org.opendevstack.apiservice.externalservice.marketplace.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CreateComponentParameter {

    private String name;
    private String type;
    private String value;

}
