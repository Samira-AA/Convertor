package com.sumax.pdfa.controller;

import java.io.IOException;

import com.sumax.pdfa.service.PdfaConversionService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/conversion-pdfa")
@RequiredArgsConstructor
public class PdfaConversionController {

    private final PdfaConversionService service;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> convert(@RequestParam("file") final MultipartFile file, @RequestParam(name = "conformanceLevel", defaultValue = "2b") final String conformanceLevel) {
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment().filename("converted-pdfa.pdf").build().toString())
            .body(this.service.convertToPdfa(file, conformanceLevel));
    }

}