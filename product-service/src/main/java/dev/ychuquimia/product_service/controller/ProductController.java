package dev.ychuquimia.product_service.controller;

import dev.ychuquimia.product_service.Service.ProductService;
import dev.ychuquimia.product_service.dto.ProductRequest;
import dev.ychuquimia.product_service.dto.ProductResponse;
import dev.ychuquimia.product_service.dto.ProductSummaryResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/products")
public class ProductController {

  private final ProductService service;

  public ProductController(ProductService service) {
    this.service = service;
  }

  @GetMapping
  public ResponseEntity<List<ProductResponse>> findAll(
      @RequestParam(required = false) String name) {
    return ResponseEntity.ok(service.findAll(name));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ProductResponse> findById(@PathVariable Long id) {
    return ResponseEntity.ok(service.findById(id));
  }

  @PostMapping
  public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ProductResponse> update(@PathVariable Long id,
      @Valid @RequestBody ProductRequest request) {
    return ResponseEntity.ok(service.update(id, request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/summaries")
  public ResponseEntity<List<ProductSummaryResponse>> findAllAndSummary() {
    return ResponseEntity.ok(service.findAllAndSumary());
  }
}