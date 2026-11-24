package com.sumax.pdfa.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sumax.pdfa.entity.Clientes;
import java.io.Serializable;
import lombok.Builder;

/** Datos de {@link Clientes}; ID y API key son de salida. El codigo se asigna solo al crear. */
@Builder
public record ClientesDto(
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) Long id,
        String codigo,
        String nombre,
        Boolean estado,
        Integer conversionesRestantes,
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        @JsonInclude(JsonInclude.Include.NON_NULL) String apiKey
) implements Serializable {
}
