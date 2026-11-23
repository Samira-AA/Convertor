package com.sumax.pdfa.mapper;

import com.sumax.pdfa.entity.ApiClient;
import com.sumax.pdfa.dto.ClientDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ClientMapper {
    @Mapping(target = "code", source = "codigo")
    @Mapping(target = "name", source = "nombre")
    @Mapping(target = "enabled", source = "estado")
    @Mapping(target = "remainingConversions", source = "conversionesRestantes")
    @Mapping(target = "apiKey", ignore = true)
    @Mapping(target = "initialConversions", ignore = true)
    ClientDto toDto(ApiClient client);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "codigo", source = "request.code")
    @Mapping(target = "nombre", expression = "java(request.name().trim())")
    @Mapping(target = "conversionesRestantes", source = "request.initialConversions")
    @Mapping(target = "estado", source = "request.enabled", defaultValue = "true")
    @Mapping(target = "apiKeyHash", source = "apiKeyHash")
    ApiClient toEntity(ClientDto request, String apiKeyHash);

    @Mapping(target = "code", source = "client.codigo")
    @Mapping(target = "name", source = "client.nombre")
    @Mapping(target = "enabled", source = "client.estado")
    @Mapping(target = "remainingConversions", source = "client.conversionesRestantes")
    @Mapping(target = "apiKey", source = "apiKey")
    @Mapping(target = "initialConversions", ignore = true)
    ClientDto toDtoWithApiKey(ApiClient client, String apiKey);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "estado", source = "enabled")
    void updateEntity(ClientDto request, @MappingTarget ApiClient client);
}
