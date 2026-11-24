package com.sumax.pdfa.service.impl;

import com.sumax.pdfa.dao.ClientDao;
import com.sumax.pdfa.dto.ClientesDto;
import com.sumax.pdfa.dto.ConversionesRestantesDto;
import com.sumax.pdfa.entity.Clientes;
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
    public ClientesDto create(ClientesDto request) {
        this.validateConversions(request.conversionesRestantes());
        String apiKey = CredentialHash.newApiKey();
        Clientes client = this.saveClient(this.mapper.toEntity(request, CredentialHash.sha256(apiKey)));
        return this.mapper.toDtoWithApiKey(client, apiKey);
    }

    @Override
    public List<ClientesDto> list() {
        return this.clientDao.findAllByOrderByIdAsc().stream().map(this.mapper::toDto).toList();
    }

    @Override
    public ClientesDto findById(long id) {
        return this.clientDao.findById(id).map(this.mapper::toDto).orElseThrow(this::clientNotFound);
    }

    @Override
    public ConversionesRestantesDto findConversionesRestantes(long id) {
        return this.clientDao.findConversionesRestantesById(id)
                .orElseThrow(this::clientNotFound);
    }

    @Override
    @Transactional
    public ClientesDto update(long id, ClientesDto request) {
        if (request.codigo() != null) {
            throw new CommercialException(HttpStatus.BAD_REQUEST, "El codigo del cliente no se puede modificar");
        }
        if (request.nombre() == null && request.estado() == null && request.conversionesRestantes() == null) {
            throw new CommercialException(HttpStatus.BAD_REQUEST, "Debe indicar nombre, estado o saldo para editar");
        }

        if (request.conversionesRestantes() != null) {
            this.validateConversions(request.conversionesRestantes());
        }
        Clientes client = this.findClientForUpdate(id);
        this.mapper.updateEntity(request, client);
        return this.mapper.toDto(this.saveClient(client));
    }

    @Override
    @Transactional
    public ClientesDto rotateKey(long id) {
        Clientes client = this.findClientForUpdate(id);
        String apiKey = CredentialHash.newApiKey();
        client.setApiKeyHash(CredentialHash.sha256(apiKey));
        return this.mapper.toDtoWithApiKey(this.saveClient(client), apiKey);
    }

    private Clientes findClientForUpdate(long id) {
        return this.clientDao.findLockedById(id).orElseThrow(this::clientNotFound);
    }

    private Clientes saveClient(Clientes client) {
        try {
            return this.clientDao.saveAndFlush(client);
        } catch (DataIntegrityViolationException exception) {
            throw new CommercialException(HttpStatus.CONFLICT, "No se pudo guardar el cliente: codigo o clave duplicados");
        }
    }

    private void validateConversions(Integer conversions) {
        if (conversions == null || conversions < 0 || conversions > 1_000_000_000) {
            throw new CommercialException(HttpStatus.BAD_REQUEST,
                    "La cantidad de conversiones debe estar entre 0 y 1000000000");
        }
    }

    private CommercialException clientNotFound() {
        return new CommercialException(HttpStatus.NOT_FOUND, "Cliente no encontrado");
    }
}
