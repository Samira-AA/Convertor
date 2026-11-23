package com.sumax.pdfa.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Clave administrativa del servicio; las claves de clientes se validan contra sus hashes en la base. */
@ConfigurationProperties("pdfa.security")
public record PdfaSecurityProperties(String adminKey) {
}
