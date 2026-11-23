package com.sumax.pdfa.controller;

import com.sumax.pdfa.dto.ClientIdentity;
import com.sumax.pdfa.service.MeteredConversionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/conversion-pdfa")
@RequiredArgsConstructor
public class PdfaConversionController {
    private final MeteredConversionService service;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> convert(@RequestParam("file") MultipartFile file,
            @RequestParam(name = "conformanceLevel", defaultValue = "2b") String conformanceLevel,
            @AuthenticationPrincipal ClientIdentity identity) {
        byte[] output = this.service.convert(identity, file, conformanceLevel);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .contentType(MediaType.APPLICATION_PDF).contentLength(output.length)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("converted-pdfa.pdf").build().toString())
                .body(output);
    }
}
