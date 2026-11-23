package com.sumax.pdfa.service.impl;

import com.sumax.pdfa.dao.ClientDao;
import com.sumax.pdfa.entity.ApiClient;
import com.sumax.pdfa.dto.ClientDto;
import com.sumax.pdfa.exception.CommercialException;
import com.sumax.pdfa.mapper.ClientMapper;
import com.sumax.pdfa.security.CredentialHash;
import com.sumax.pdfa.service.ClientService;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientServiceImpl implements ClientService {
    private final ClientDao clientDao;
    private final ClientMapper mapper;

    @Override
    @Transactional
    public ClientDto create(ClientDto request) {
        this.validateCreateRequest(request);
        String key = CredentialHash.newApiKey();
        try {
            ApiClient client = this.clientDao.saveAndFlush(this.mapper.toEntity(request, CredentialHash.sha256(key)));
            return this.mapper.toDtoWithApiKey(client, key);
        } catch (DataIntegrityViolationException exception) {
            throw new CommercialException(HttpStatus.CONFLICT, "Ya existe el código del cliente");
        }
    }

    @Override
    public List<ClientDto> list() {
        return this.clientDao.findAllByOrderByIdAsc().stream().map(this.mapper::toDto).toList();
    }

    @Override
    public ClientDto consumption(long id) {
        return this.clientDao.findById(id).map(this.mapper::toDto).orElseThrow(
                () -> new CommercialException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));
    }

    private void validateCreateRequest(ClientDto request) {
        if (request.code() == null || !request.code().matches("[a-z0-9][a-z0-9-]{0,63}")) {
            throw new CommercialException(HttpStatus.BAD_REQUEST,
                    "El código debe tener entre 1 y 64 caracteres: minúsculas, números y guiones");
        }
        if (request.name() == null || request.name().isBlank() || request.name().length() > 150) {
            throw new CommercialException(HttpStatus.BAD_REQUEST,
                    "El nombre es obligatorio y debe tener como máximo 150 caracteres");
        }
        if (request.initialConversions() == null || request.initialConversions() < 0
                || request.initialConversions() > 1_000_000_000L) {
            throw new CommercialException(HttpStatus.BAD_REQUEST,
                    "La cuota inicial debe estar entre 0 y 1000000000 conversiones");
        }
    }

    @Override
    @Transactional
    public ClientDto rotateKey(long id) {
        ApiClient client = this.clientDao.lockClient(id).orElseThrow(
                () -> new CommercialException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));
        String key = CredentialHash.newApiKey();
        client.setApiKeyHash(CredentialHash.sha256(key));
        return this.mapper.toDtoWithApiKey(this.clientDao.saveAndFlush(client), key);
    }

    @Override
    @Transactional
    public ClientDto setStatus(long id, ClientDto request) {
        if (request.enabled() == null || request.code() != null || request.name() != null
                || request.initialConversions() != null) {
            throw new CommercialException(HttpStatus.BAD_REQUEST, "Cliente o estado no válido");
        }
        ApiClient client = this.clientDao.lockClient(id).orElseThrow(
                () -> new CommercialException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));
        this.mapper.updateEntity(request, client);
        return this.mapper.toDto(this.clientDao.saveAndFlush(client));
    }
}
