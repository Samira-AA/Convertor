package com.sumax.pdfa.sdk.autoconfigure;

import com.sumax.pdfa.sdk.PdfaClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/** Registra el cliente del SDK con la URL y clave configuradas en la aplicacion consumidora. */
@AutoConfiguration
@ConditionalOnClass(PdfaClient.class)
public class PdfaClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(PdfaClient.class)
    public PdfaClient pdfaClient(@Value("${PDFA_URL}") String baseUrl,
                               @Value("${PDFA_API_KEY}") String apiKey) {
        return new PdfaClient(baseUrl, apiKey);
    }
}
