package com.sumax.pdfa.sdk;

import java.io.Serial;

/** Error de conversión remota o de transporte. No contiene claves ni cuerpos HTTP. */
public final class PdfaClientException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int statusCode;

    PdfaClientException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    PdfaClientException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 0;
    }

    /** Código HTTP recibido, o cero si la petición no obtuvo una respuesta HTTP. */
    public int getStatusCode() {
        return this.statusCode;
    }
}
