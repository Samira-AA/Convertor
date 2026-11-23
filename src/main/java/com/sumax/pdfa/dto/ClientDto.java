package com.sumax.pdfa.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sumax.pdfa.entity.ApiClient;
import java.io.Serializable;
import lombok.Builder;

/** Datos de {@link ApiClient}; ID, saldo y API key son de salida y no se aceptan del consumidor. */
@Builder
public record ClientDto(
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) Long id,
        String code,
        String name,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) Long initialConversions,
        Boolean enabled,
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) Long remainingConversions,
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        @JsonInclude(JsonInclude.Include.NON_NULL) String apiKey
) implements Serializable {
}
