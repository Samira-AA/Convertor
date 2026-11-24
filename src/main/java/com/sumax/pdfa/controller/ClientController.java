package com.sumax.pdfa.controller;

import com.sumax.framework.models.rest.ApiResponse;
import com.sumax.pdfa.dto.ClientesDto;
import com.sumax.pdfa.dto.ConversionesRestantesDto;
import com.sumax.pdfa.dto.IdentidadCliente;
import com.sumax.pdfa.service.ClientService;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/clientes")
public class ClientController {
    private final ClientService clientService;

    @PostMapping
    public ResponseEntity<ApiResponse> create(@RequestBody ClientesDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.create("Cliente creado", this.clientService.create(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse> list() {
        return ResponseEntity.ok(ApiResponse.ok("Clientes consultados", this.clientService.list()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> findById(@PathVariable long id) {
        return ResponseEntity.ok(ApiResponse.ok("Cliente consultado", this.clientService.findById(id)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse> update(@PathVariable long id, @RequestBody ClientesDto request) {
        return ResponseEntity.ok(ApiResponse.ok("Cliente actualizado", this.clientService.update(id, request)));
    }

    @PostMapping("/{id}/api-key")
    public ResponseEntity<ApiResponse> rotateKey(@PathVariable long id) {
        return ResponseEntity.ok(ApiResponse.ok("API key renovada", this.clientService.rotateKey(id)));
    }

    @GetMapping("/conversiones-restantes")
    public ResponseEntity<ConversionesRestantesDto> consumption(@AuthenticationPrincipal IdentidadCliente identity) {
        return ResponseEntity.ok(this.clientService.findConversionesRestantes(identity.id()));
    }
}
