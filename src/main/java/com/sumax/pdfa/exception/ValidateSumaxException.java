package com.sumax.pdfa.exception;

import lombok.Getter;

@Getter
public class ValidateSumaxException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final EnumPdfaError error;

    public ValidateSumaxException(final EnumPdfaError error) {
        super(error.getMensaje());
        this.error = error;
    }

    public ValidateSumaxException(final EnumPdfaError error, final String detalle) {
        super(detalle);
        this.error = error;
    }
}