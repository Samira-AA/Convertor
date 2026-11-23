package com.sumax.pdfa.controller;

import com.sumax.pdfa.dto.ClientDto;
import com.sumax.pdfa.dto.ClientIdentity;
import com.sumax.pdfa.service.ClientService;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clients")
public class ClientController {
    private final ClientService service;

    @PostMapping
    public ResponseEntity<ClientDto> create(@RequestBody ClientDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).cacheControl(CacheControl.noStore()).body(this.service.create(request));
    }

    @GetMapping
    public List<ClientDto> list() {
        return this.service.list();
    }

    @GetMapping("/me/consumption")
    public ClientDto consumption(@AuthenticationPrincipal ClientIdentity identity) {
        return this.service.consumption(identity.clientId());
    }

    @GetMapping("/{id}/consumption")
    public ClientDto consumption(@PathVariable long id) {
        return this.service.consumption(id);
    }

    @PostMapping("/{id}/api-keys")
    public ResponseEntity<ClientDto> rotateKey(@PathVariable long id) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(this.service.rotateKey(id));
    }

    @PatchMapping("/{id}/status")
    public ClientDto setStatus(@PathVariable long id, @RequestBody ClientDto request) {
        return this.service.setStatus(id, request);
    }
}
