package com.sumax.pdfa.mapper;

import com.sumax.pdfa.entity.Clientes;
import com.sumax.pdfa.dto.ClientesDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ClientMapper {
    @Mapping(target = "apiKey", ignore = true)
    ClientesDto toDto(Clientes client);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estado", source = "request.estado", defaultValue = "true")
    @Mapping(target = "apiKeyHash", source = "apiKeyHash")
    Clientes toEntity(ClientesDto request, String apiKeyHash);

    @Mapping(target = "apiKey", source = "apiKey")
    ClientesDto toDtoWithApiKey(Clientes client, String apiKey);

    @BeanMapping(ignoreByDefault = true, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "nombre", source = "nombre")
    @Mapping(target = "estado", source = "estado")
    @Mapping(target = "conversionesRestantes", source = "conversionesRestantes")
    void updateEntity(ClientesDto request, @MappingTarget Clientes client);
}
