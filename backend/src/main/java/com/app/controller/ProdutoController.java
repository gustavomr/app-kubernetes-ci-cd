package com.app.controller;

import com.app.model.Produto;
import com.app.repository.ProdutoRepository;
import com.app.service.RocketflagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    private static final Logger log = LoggerFactory.getLogger(ProdutoController.class);

    private final ProdutoRepository repository;
    private final RocketflagService rocketflagService;

    public ProdutoController(ProdutoRepository repository, RocketflagService rocketflagService) {
        this.repository = repository;
        this.rocketflagService = rocketflagService;
    }

    @GetMapping
    public ResponseEntity<List<Produto>> listar() {
        var flag = rocketflagService.isFeatureEnabled();
        log.info("RocketFlag add-produto={}", flag);
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
