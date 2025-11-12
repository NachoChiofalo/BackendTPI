package com.tpi.localizaciones.controller;

import com.tpi.localizaciones.entity.Deposito;
import com.tpi.localizaciones.service.DepositoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/depositos")
@RequiredArgsConstructor
public class DepositoController {

    private final DepositoService depositoService;

    @GetMapping
    public ResponseEntity<List<Deposito>> getAllDepositos() {
        List<Deposito> depositos = depositoService.findAll();
        return ResponseEntity.ok(depositos);
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countDepositos() {
        return ResponseEntity.ok(depositoService.count());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Deposito> getDepositoById(@PathVariable("id") Integer id) {
        return depositoService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

