package com.revenerg.client.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = SuccessProvisionResponse.class, name="success"),
        @JsonSubTypes.Type(value = ErrorProvisionResponse.class, name="error")
})
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class ProvisionResponse {
    private String type;
}
