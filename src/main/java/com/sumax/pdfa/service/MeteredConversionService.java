package com.sumax.pdfa.service;

import com.sumax.pdfa.dto.ClientIdentity;
import org.springframework.web.multipart.MultipartFile;

/** Convierte y descuenta el saldo del cliente tras validar el resultado, sin historial persistente. */
public interface MeteredConversionService {
    byte[] convert(ClientIdentity identity, MultipartFile file, String profile);
}
