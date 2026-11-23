package com.sumax.pdfa.sdk;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * Cliente reutilizable de Converter, independiente de Spring.
 * Configurar una URL base que termine en /api y la API key del cliente.
 * Envía el archivo por multipart y devuelve el PDF/A validado por el servidor.
 */
public final class PdfaClient {
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(330);
    private static final JsonFactory JSON_FACTORY = new JsonFactory();

    private final HttpClient httpClient;
    private final URI conversionEndpoint;
    private final String apiKey;
    private final Duration timeout;

    public PdfaClient(String baseUrl, String apiKey) {
        this(baseUrl, apiKey, DEFAULT_TIMEOUT);
    }

    public PdfaClient(String baseUrl, String apiKey, Duration timeout) {
        this.conversionEndpoint = conversionEndpoint(baseUrl);
        if (apiKey == null || apiKey.isBlank() || apiKey.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("La API key es obligatoria y no debe contener caracteres de control");
        }
        this.apiKey = apiKey.trim();
        this.timeout = Objects.requireNonNull(timeout, "timeout es obligatorio");
        if (timeout.isNegative() || timeout.isZero()) {
            throw new IllegalArgumentException("timeout debe ser mayor que cero");
        }
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }

    /**
     * Convierte un PDF local. Una conversión exitosa puede consumir saldo.
     * No reintenta peticiones en la aplicación. Un fallo de red puede dejar
     * indeterminado si el servidor completó y cobró la conversión.
     *
     * @param file ruta del PDF de entrada
     * @param profile perfil PDF/A solicitado
     * @return bytes del PDF/A; el consumidor decide dónde guardarlos
     * @throws IllegalArgumentException si el archivo no es legible o está vacío
     * @throws PdfaClientException ante error HTTP, de transporte o respuesta no PDF
     */
    public byte[] convert(Path file, PdfaProfile profile) {
        Objects.requireNonNull(file, "file es obligatorio");
        Objects.requireNonNull(profile, "profile es obligatorio");
        if (!Files.isRegularFile(file) || !Files.isReadable(file)) {
            throw new IllegalArgumentException("El PDF debe ser un archivo local legible");
        }
        try {
            if (Files.size(file) == 0) {
                throw new IllegalArgumentException("El PDF no puede estar vacío");
            }
            String boundary = "sumax-pdfa-" + UUID.randomUUID();
            HttpRequest request = HttpRequest.newBuilder(this.conversionEndpoint)
                    .timeout(this.timeout)
                    .header("X-API-Key", this.apiKey)
                    .header("Accept", "application/pdf")
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(multipart(file, profile, boundary))
                    .build();
            HttpResponse<byte[]> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200) {
                throw httpError(response);
            }
            byte[] output = response.body();
            String contentType = response.headers().firstValue("Content-Type").orElse("")
                    .split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
            if (!"application/pdf".equals(contentType) || output.length < 5
                    || !"%PDF-".equals(new String(output, 0, 5, StandardCharsets.US_ASCII))) {
                throw new PdfaClientException(200, "Converter devolvió una respuesta que no es un PDF");
            }
            return output;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new PdfaClientException("La solicitud de conversión fue interrumpida", exception);
        } catch (IOException exception) {
            throw new PdfaClientException("No se pudo completar la comunicación con Converter", exception);
        }
    }

    private static HttpRequest.BodyPublisher multipart(Path file, PdfaProfile profile, String boundary)
            throws IOException {
        String prefix = "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"conformanceLevel\"\r\n\r\n"
                + profile.value() + "\r\n"
                + "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"file\"; filename=\"input.pdf\"\r\n"
                + "Content-Type: application/pdf\r\n\r\n";
        return HttpRequest.BodyPublishers.concat(
                HttpRequest.BodyPublishers.ofString(prefix, StandardCharsets.UTF_8),
                HttpRequest.BodyPublishers.ofFile(file),
                HttpRequest.BodyPublishers.ofString("\r\n--" + boundary + "--\r\n", StandardCharsets.UTF_8));
    }

    private static URI conversionEndpoint(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("La URL base de Converter es obligatoria");
        }
        URI base;
        try {
            base = URI.create(baseUrl.trim());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("La URL base de Converter no es válida");
        }
        if (!("https".equalsIgnoreCase(base.getScheme()) || "http".equalsIgnoreCase(base.getScheme()))
                || base.getHost() == null || base.getUserInfo() != null
                || base.getQuery() != null || base.getFragment() != null) {
            throw new IllegalArgumentException("La URL debe ser HTTP(S), sin credenciales, query ni fragmento");
        }
        String normalized = base.toString().replaceAll("/+$", "");
        return URI.create(normalized + "/conversion-pdfa");
    }

    private static PdfaClientException httpError(HttpResponse<byte[]> response) {
        int statusCode = response.statusCode();
        String detail = problemDetail(response.body());
        if (detail != null) {
            return new PdfaClientException(statusCode, detail);
        }
        String message = switch (statusCode) {
            case 400 -> "Converter rechazó el archivo o los parámetros";
            case 401 -> "API key no válida o cliente no habilitado";
            case 403 -> "El cliente no tiene permiso para convertir";
            case 413 -> "El archivo supera el tamaño permitido";
            default -> "Converter devolvió un error HTTP " + statusCode;
        };
        return new PdfaClientException(statusCode, message);
    }

    /** Lee solo el detalle publico del error; nunca propaga HTML ni el cuerpo completo. */
    private static String problemDetail(byte[] body) {
        if (body.length == 0 || body.length > 65536) {
            return null;
        }
        try (JsonParser parser = JSON_FACTORY.createParser(body)) {
            if (parser.nextToken() != JsonToken.START_OBJECT) {
                return null;
            }
            String detail = null;
            while (parser.nextToken() == JsonToken.FIELD_NAME) {
                String fieldName = parser.currentName();
                JsonToken valueToken = parser.nextToken();
                if ("detail".equals(fieldName) && valueToken == JsonToken.VALUE_STRING) {
                    detail = parser.getText();
                }
                parser.skipChildren();
            }
            return parser.currentToken() == JsonToken.END_OBJECT && parser.nextToken() == null
                    && detail != null && !detail.isBlank() ? detail : null;
        } catch (IOException exception) {
            return null;
        }
    }
}
