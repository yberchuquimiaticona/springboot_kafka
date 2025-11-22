package dev.ychuquimia.product_service.controller;

import dev.ychuquimia.product_service.Service.CategoryService;
import dev.ychuquimia.product_service.dto.CategoryRequest;
import dev.ychuquimia.product_service.dto.CategoryResponse;
import dev.ychuquimia.product_service.dto.ProductResponse;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

  private final CategoryService service;

  public CategoryController(CategoryService service) {
    this.service = service;
  }

  @GetMapping
  public ResponseEntity<List<CategoryResponse>> findAll() {
    return ResponseEntity.ok(service.findAll());
  }

  @PostMapping
  public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
  }

  @GetMapping("/{id}/products")
  public ResponseEntity<List<ProductResponse>> findProducts(@PathVariable Long id) {
    return ResponseEntity.ok(service.findProducts(id));
  }
}
