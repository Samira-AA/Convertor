package com.sumax.pdfa.service;

import org.springframework.web.multipart.MultipartFile;

public interface PdfaConversionService {

    byte[] convertToPdfa(MultipartFile file, String conformanceLevel);

}