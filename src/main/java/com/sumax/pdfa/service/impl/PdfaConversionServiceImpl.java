package com.sumax.pdfa.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Locale;

import com.sumax.pdfa.dao.ClientDao;
import com.sumax.pdfa.dto.IdentidadCliente;
import com.sumax.pdfa.entity.Clientes;
import com.sumax.pdfa.exception.CommercialException;
import com.sumax.pdfa.exception.EnumPdfaError;
import com.sumax.pdfa.exception.ValidateSumaxException;
import com.sumax.pdfa.service.PdfaConversionService;
import jakarta.annotation.PostConstruct;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import org.verapdf.core.EncryptedPdfException;
import org.verapdf.core.ModelParsingException;
import org.verapdf.core.ValidationException;
import org.verapdf.gf.foundry.VeraGreenfieldFoundryProvider;
import org.verapdf.pdfa.Foundries;
import org.verapdf.pdfa.PDFAParser;
import org.verapdf.pdfa.PDFAValidator;
import org.verapdf.pdfa.flavours.PDFAFlavour;
import org.verapdf.pdfa.results.ValidationResult;
import org.verapdf.pdfa.validation.validators.ValidatorConfigBuilder;

import lombok.extern.slf4j.Slf4j;
import stirling.software.officeconvert.pdfa.PdfALevel;
import stirling.software.officeconvert.pdfa.PdfToPdfA;

@Slf4j
@Service
public class PdfaConversionServiceImpl implements PdfaConversionService {
    private final ClientDao clientDao;
    private final TransactionTemplate transactions;

    @Value("${pdfa.max-pages}")
    private int maxPages;

    @Value("${pdfa.timeout-seconds}")
    private long timeoutSeconds;

    public PdfaConversionServiceImpl(ClientDao clientDao, PlatformTransactionManager transactionManager) {
        this.clientDao = clientDao;
        this.transactions = new TransactionTemplate(transactionManager);
    }

    @PostConstruct
    private void initVeraPdf() {
        VeraGreenfieldFoundryProvider.initialise();
    }

    @Override
    public byte[] convert(IdentidadCliente identity, MultipartFile file, String requestedProfile) {
        String profile = requestedProfile == null ? "" : requestedProfile.trim().toLowerCase(Locale.ROOT);
        PdfALevel pdfaLevel = this.getPdfaLevel(profile);
        if (file == null || file.isEmpty()) {
            throw new ValidateSumaxException(EnumPdfaError.ER000001);
        }
        if (file.getSize() > 20L * 1024 * 1024) {
            throw new ValidateSumaxException(EnumPdfaError.ER000002);
        }
        Clientes client = this.clientDao.findById(identity.id()).orElseThrow(this::clientNotFound);
        this.validateClient(client);

        byte[] output = this.convertFile(file, profile, pdfaLevel);
        if (output.length == 0) {
            throw new ValidateSumaxException(EnumPdfaError.ER000006);
        }
        this.discountConversion(identity.id());
        return output;
    }

    private byte[] convertFile(MultipartFile file, String profile, PdfALevel pdfaLevel) {
        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("pdfa-");
            final Path inputPath = tempDir.resolve("input.pdf");
            final Path outputPath = tempDir.resolve("output.pdf");
            file.transferTo(inputPath);

            this.executeConversion(inputPath, outputPath, pdfaLevel);
            this.validatePdfa(outputPath, profile);

            return Files.readAllBytes(outputPath);
        } catch (IOException _) {
            throw new ValidateSumaxException(EnumPdfaError.ER000006);
        } finally {
            if (tempDir != null) {
                FileSystemUtils.deleteRecursively(tempDir.toFile());
            }
        }
    }

    private void discountConversion(long clientId) {
        this.transactions.executeWithoutResult(transaction -> {
            Clientes client = this.clientDao.findLockedById(clientId).orElseThrow(this::clientNotFound);
            this.validateClient(client);
            if (client.getConversionesRestantes() != null) {
                client.setConversionesRestantes(client.getConversionesRestantes() - 1);
            }
        });
    }

    private void validateClient(Clientes client) {
        if (!client.isEstado()) {
            throw new CommercialException(HttpStatus.FORBIDDEN, "Cliente deshabilitado");
        }
        if (client.getConversionesRestantes() != null && client.getConversionesRestantes() <= 0) {
            throw new CommercialException(HttpStatus.TOO_MANY_REQUESTS,
                    "No quedan conversiones disponibles para agregar documentos");
        }
    }

    private CommercialException clientNotFound() {
        return new CommercialException(HttpStatus.NOT_FOUND, "Cliente no encontrado");
    }

    private void executeConversion(final Path inputPath, final Path outputPath, final PdfALevel level) {
        try {
            final PdfToPdfA.Options options = PdfToPdfA.Options.defaults()
                .level(level)
                .maxPages(this.maxPages)
                .timeout(Duration.ofSeconds(this.timeoutSeconds));
            final PdfToPdfA.Result result = PdfToPdfA.convert(inputPath, outputPath, options);
            result.warnings().forEach(warning -> log.info("advertencia conversion {}", warning));
        } catch (IOException e) {
            log.warn("Conversión fallida: {}", e.getMessage());
            if (e instanceof InvalidPasswordException || e.getCause() instanceof InvalidPasswordException) {
                throw new ValidateSumaxException(EnumPdfaError.ER000004);
            }
            throw new ValidateSumaxException(EnumPdfaError.ER000005, e.getMessage());
        }
    }

    private void validatePdfa(final Path outputPath, final String level) {
        final PDFAFlavour flavour = PDFAFlavour.fromString(level);
        final var validationConfig = ValidatorConfigBuilder.defaultBuilder().flavour(flavour)
                .recordPasses(false).showErrorMessages(false).build();
        try (PDFAParser parser = Foundries.defaultInstance().createParser(outputPath.toFile(), flavour);
             PDFAValidator validator = Foundries.defaultInstance().createValidator(validationConfig)) {

            final ValidationResult result = validator.validate(parser);
            if (!result.isCompliant()) {
                result.getFailedChecks()
                    .forEach((rule, times) -> log.warn("Regla incumplida {},{}", rule, times));
                throw new ValidateSumaxException(EnumPdfaError.ER000007);
            }
            log.info("validado correctamente con veraPDF {}", level);
        } catch (IOException | ValidationException | ModelParsingException | EncryptedPdfException e) {
            log.warn("error {}", e.getMessage());
            throw new ValidateSumaxException(EnumPdfaError.ER000007);
        }
    }

    private PdfALevel getPdfaLevel(final String level) {
        return switch (level) {
            case "1b" -> PdfALevel.A1B;
            case "2b" -> PdfALevel.A2B;
            default -> throw new ValidateSumaxException(EnumPdfaError.ER000003, "Nivel no soportado: Use 1b o 2b");
        };
    }
}
