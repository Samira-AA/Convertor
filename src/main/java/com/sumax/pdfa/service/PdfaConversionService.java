package com.sumax.pdfa.service;

import com.sumax.pdfa.dto.IdentidadCliente;
import org.springframework.web.multipart.MultipartFile;

/** Convierte, valida el PDF/A y descuenta el saldo del cliente al finalizar correctamente. */
public interface PdfaConversionService {

    byte[] convert(IdentidadCliente identity, MultipartFile file, String conformanceLevel);

}
