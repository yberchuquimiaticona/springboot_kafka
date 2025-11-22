package dev.ychuquimia.product_service.Service;

import dev.ychuquimia.product_service.dto.CategoryRequest;
import dev.ychuquimia.product_service.dto.CategoryResponse;
import dev.ychuquimia.product_service.dto.ProductResponse;
import dev.ychuquimia.product_service.exception.CategoryAlreadyExistsException;
import dev.ychuquimia.product_service.exception.ResourceNotFoundException;
import dev.ychuquimia.product_service.mapper.ProductMapper;
import dev.ychuquimia.product_service.model.Category;
import dev.ychuquimia.product_service.repository.CategoryRepository;
import dev.ychuquimia.product_service.repository.ProductRepository;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional(readOnly = true)
public class CategoryService {

  private final CategoryRepository categoryRepository;
  private final ProductRepository productRepository;

  public CategoryService(CategoryRepository categoryRepository, ProductRepository productRepository) {
    this.categoryRepository = categoryRepository;
    this.productRepository = productRepository;
  }

  public List<CategoryResponse> findAll() {
    return categoryRepository.findAll().stream()
        .map(category -> new CategoryResponse(category.getId(), category.getName(), category.getDescription()))
        .toList();
  }

  @Transactional
  public CategoryResponse create(CategoryRequest request) {
    if (categoryRepository.existsByNameIgnoreCase(request.name())) {
      throw new CategoryAlreadyExistsException(request.name());
    }
    Category category = new Category();
    category.setName(request.name());
    category.setDescription(request.description());
    Category saved = categoryRepository.save(category);
    return new CategoryResponse(saved.getId(), saved.getName(), saved.getDescription());
  }

  public List<ProductResponse> findProducts(Long id) {
    Category category = categoryRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Categoría " + id + " no encontrada"));
    return productRepository.findByCategoryId(category.getId()).stream()
        .map(ProductMapper::toResponse)
        .toList();
  }
}
