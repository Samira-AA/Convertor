package com.sumax.pdfa.security;

import com.sumax.pdfa.dao.ClientDao;
import com.sumax.pdfa.dto.IdentidadCliente;
import com.sumax.pdfa.entity.Clientes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public final class ClientAuthenticationFilter extends OncePerRequestFilter {
    private final ClientDao clientDao;
    private final PdfaSecurityProperties properties;

    public ClientAuthenticationFilter(ClientDao clientDao, PdfaSecurityProperties properties) {
        this.clientDao = clientDao;
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain)
            throws ServletException, IOException {
        String apiKey = request.getHeader("X-API-Key");
        String adminKey = request.getHeader("X-Admin-Key");
        int methods = (apiKey != null ? 1 : 0) + (adminKey != null ? 1 : 0)
                + (request.getHeader("Authorization") != null ? 1 : 0);
        if (methods > 1) {
            response.sendError(400, "Envíe un único mecanismo de autenticación");
            return;
        }
        if (apiKey != null) {
            Clientes client = apiKey.length() > 200 ? null : this.clientDao.findByApiKeyHash(CredentialHash.sha256(apiKey))
                    .filter(Clientes::isEstado).orElse(null);
            if (client == null) {
                response.sendError(401, "Clave de cliente no válida");
                return;
            }
            this.authenticate(new IdentidadCliente(client.getId()), "PDFA_CLIENT");
        } else if (adminKey != null) {
            String expected = this.properties.adminKey();
            if (expected == null || expected.length() < 32 || adminKey.length() > 200
                    || !MessageDigest.isEqual(CredentialHash.sha256(expected).getBytes(StandardCharsets.US_ASCII),
                    CredentialHash.sha256(adminKey).getBytes(StandardCharsets.US_ASCII))) {
                response.sendError(401, "Credencial de administración no válida");
                return;
            }
            this.authenticate("pdfa-admin", "PDFA_ADMIN");
        }
        chain.doFilter(request, response);
    }

    private void authenticate(Object principal, String authority) {
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(principal, null,
                List.of(new SimpleGrantedAuthority(authority))));
        SecurityContextHolder.setContext(context);
    }
}
