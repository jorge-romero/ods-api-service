package org.opendevstack.apiservice.project.mapper;

import org.mapstruct.Mapper;
import org.opendevstack.apiservice.project.model.CreateComponentResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Mapper(componentModel = "spring")
public interface ComponentResponseMapper {

    default ResponseEntity<CreateComponentResponse> toResponseEntity(CreateComponentResponse response) {
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getHttpStatus()));
    }
}
