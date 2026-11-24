package com.sumax.pdfa.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@RestControllerAdvice
public class ResponseExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleUnreadableBody(HttpMessageNotReadableException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "El cuerpo JSON contiene un formato o tipo de dato no valido");
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(CommercialException.class)
    public ResponseEntity<ProblemDetail> handleCommercial(CommercialException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(exception.status(), exception.getMessage());
        problem.setTitle("Solicitud comercial rechazada");
        return ResponseEntity.status(exception.status()).body(problem);
    }

    @ExceptionHandler(ValidateSumaxException.class)
    public ResponseEntity<ProblemDetail> handleValidate(final ValidateSumaxException ex) {
        return this.build(ex.getError(), ex.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ProblemDetail> handleMaxUpload() {
        return this.build(EnumPdfaError.ER000002, null);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ProblemDetail> handleMissingPart() {
        return this.build(EnumPdfaError.ER000001, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGeneric(final Exception ex) {
        if (ex instanceof ErrorResponse errorResponse) {
            return ResponseEntity.status(errorResponse.getStatusCode()).body(errorResponse.getBody());
        }
        return this.build(EnumPdfaError.ER000006, null);
    }

    private ResponseEntity<ProblemDetail> build(final EnumPdfaError error, final String detalle) {
        final ProblemDetail problem = ProblemDetail.forStatusAndDetail(error.getStatus(),
            detalle != null ? detalle : error.getMensaje());
        problem.setTitle(error.getMensaje());
        problem.setProperty("codigo", error.getCodigo());
        return ResponseEntity.status(error.getStatus()).body(problem);
    }
}
