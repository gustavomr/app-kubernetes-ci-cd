package com.app.controller;

import com.app.model.Produto;
import com.app.repository.ProdutoRepository;
import com.app.service.RocketflagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    private final ProdutoRepository repository;
    private final RocketflagService rocketflagService;

    public ProdutoController(ProdutoRepository repository, RocketflagService rocketflagService) {
        this.repository = repository;
        this.rocketflagService = rocketflagService;
    }

    @GetMapping
    public ResponseEntity<List<Produto>> listar() {
        return ResponseEntity.ok(repository.findAll());
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Produto produto) {
        if (!rocketflagService.isFeatureEnabled()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(repository.save(produto));
    }
}
