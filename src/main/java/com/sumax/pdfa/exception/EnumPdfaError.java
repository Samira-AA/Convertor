package com.sumax.pdfa.exception;

import org.springframework.http.HttpStatus;

import com.sumax.framework.exception.GenericError;
import com.sumax.framework.exception.enums.TypeException;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EnumPdfaError implements GenericError {

    ER000001("PDA000001", "Debe enviar el archivo PDF", TypeException.A, HttpStatus.BAD_REQUEST),
    ER000002("PDA000002", "El archivo supera el tamaño máximo permitido (20 MB)", TypeException.A, HttpStatus.PAYLOAD_TOO_LARGE),
    ER000003("PDA000003", "Nivel PDF/A no soportado", TypeException.A, HttpStatus.BAD_REQUEST),
    ER000004("PDA000004", "El PDF está protegido con contraseña y no se puede convertir", TypeException.A, HttpStatus.UNPROCESSABLE_ENTITY),
    ER000005("PDA000005", "No se pudo convertir el PDF a PDF/A", TypeException.E, HttpStatus.UNPROCESSABLE_ENTITY),
    ER000006("PDA000006", "Error interno del servicio", TypeException.E, HttpStatus.INTERNAL_SERVER_ERROR),
    ER000007("PDA000007", "El resultado no cumple el perfil PDF/A solicitado", TypeException.E, HttpStatus.UNPROCESSABLE_ENTITY);

    private final String codigo;
    private final String mensaje;
    private final TypeException type;
    private final HttpStatus status;

    @Override
    public String getTitulo() {
        return "";
    }
}