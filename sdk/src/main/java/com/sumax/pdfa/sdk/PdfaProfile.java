package com.sumax.pdfa.sdk;

/** Perfiles habilitados por la API de conversión. */
public enum PdfaProfile {
    PDFA_1B("1b"),
    PDFA_2B("2b");

    private final String value;

    PdfaProfile(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }
}
