package com.koch.anomaly;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/kilns")
public class KilnController {

    private final KilnService kilnService;

    public KilnController(KilnService kilnService) {
        this.kilnService = kilnService;
    }

    @GetMapping
    public List<KilnResponse> getAllKilns() {
        return kilnService.getAllKilns();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('GATEWAY_ADMIN')")
    public ResponseEntity<KilnResponse> createKiln(@RequestBody @Valid KilnRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(kilnService.createKiln(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('GATEWAY_ADMIN')")
    public KilnResponse updateKiln(@PathVariable UUID id, @Valid @RequestBody KilnRequest request) {
        return kilnService.updateKiln(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('GATEWAY_ADMIN')")
    public void deleteKiln(@PathVariable UUID id) {
        kilnService.deleteKiln(id);
    }
}
