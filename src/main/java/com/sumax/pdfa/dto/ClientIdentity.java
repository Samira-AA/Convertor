package com.sumax.pdfa.dto;

import com.sumax.pdfa.entity.ApiClient;

/** Identidad autenticada correspondiente a un {@link ApiClient}. */
public record ClientIdentity(long clientId) {
}
