package com.sumax.pdfa.service.impl;

import com.sumax.pdfa.dao.ClientDao;
import com.sumax.pdfa.entity.ApiClient;
import com.sumax.pdfa.exception.CommercialException;
import com.sumax.pdfa.exception.EnumPdfaError;
import com.sumax.pdfa.exception.ValidateSumaxException;
import com.sumax.pdfa.dto.ClientIdentity;
import com.sumax.pdfa.service.MeteredConversionService;
import com.sumax.pdfa.service.PdfaConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Set;

@Service
public class MeteredConversionServiceImpl implements MeteredConversionService {
    private final ClientDao clientDao;
    private final PdfaConversionService engine;
    private final TransactionTemplate transactions;

    public MeteredConversionServiceImpl(ClientDao clientDao, PdfaConversionService engine,
            PlatformTransactionManager transactionManager) {
        this.clientDao = clientDao;
        this.engine = engine;
        this.transactions = new TransactionTemplate(transactionManager);
    }

    @Override
    public byte[] convert(ClientIdentity identity, MultipartFile file, String requestedProfile) {
        String profile = requestedProfile == null ? "" : requestedProfile.trim().toLowerCase(Locale.ROOT);
        if (!Set.of("1b", "2b").contains(profile)) {
            throw new ValidateSumaxException(EnumPdfaError.ER000003);
        }
        if (file == null || file.isEmpty()) {
            throw new ValidateSumaxException(EnumPdfaError.ER000001);
        }
        if (file.getSize() > 20L * 1024 * 1024) {
            throw new ValidateSumaxException(EnumPdfaError.ER000002);
        }
        ApiClient client = this.clientDao.findById(identity.clientId()).orElseThrow(this::clientNotFound);
        this.validateClient(client);

        // La conversión no mantiene transacciones ni bloqueos de base de datos abiertos.
        byte[] output = this.engine.convertToPdfa(file, profile);
        if (output == null || output.length == 0) {
            throw new ValidateSumaxException(EnumPdfaError.ER000006);
        }
        // Comprobar nuevamente bajo bloqueo evita sobregirar el último saldo por concurrencia.
        this.transactions.executeWithoutResult(transaction -> {
            ApiClient current = this.clientDao.lockClient(identity.clientId()).orElseThrow(this::clientNotFound);
            this.validateClient(current);
            if (current.getConversionesRestantes() != null) {
                current.setConversionesRestantes(current.getConversionesRestantes() - 1);
            }
        });
        return output;
    }

    private void validateClient(ApiClient client) {
        if (!client.isEstado()) {
            throw new CommercialException(HttpStatus.FORBIDDEN, "Cliente deshabilitado");
        }
        if (client.getConversionesRestantes() != null && client.getConversionesRestantes() <= 0) {
            throw new CommercialException(HttpStatus.TOO_MANY_REQUESTS,
                    "No quedan conversiones disponibles para agregar documentos");
        }
    }

    private CommercialException clientNotFound() {
        return new CommercialException(HttpStatus.NOT_FOUND, "Cliente no encontrado");
    }
}
