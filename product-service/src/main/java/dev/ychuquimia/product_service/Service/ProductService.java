package dev.ychuquimia.product_service.Service;

import dev.ychuquimia.product_service.dto.ProductRequest;
import dev.ychuquimia.product_service.dto.ProductResponse;
import dev.ychuquimia.product_service.dto.ProductSummaryResponse;
import dev.ychuquimia.product_service.exception.ResourceNotFoundException;
import dev.ychuquimia.product_service.kafka.event.ProductCreatedEvent;
import dev.ychuquimia.product_service.kafka.producer.ProductEventProducer;
import dev.ychuquimia.product_service.mapper.ProductMapper;
import dev.ychuquimia.product_service.model.Category;
import dev.ychuquimia.product_service.model.Product;
import dev.ychuquimia.product_service.repository.CategoryRepository;
import dev.ychuquimia.product_service.repository.ProductRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional(readOnly = true)
public class ProductService {

  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;
  private final ProductEventProducer productEventProducer;

  public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository,
      ProductEventProducer productEventProducer) {
    this.productRepository = productRepository;
    this.categoryRepository = categoryRepository;
    this.productEventProducer = productEventProducer;
  }
  public List<ProductResponse> findAll(String name) {
    List<Product> products = (name == null || name.isBlank())
        ? productRepository.findAll()
        : productRepository.findByNameContainingIgnoreCase(name);
    return products.stream()
        .map(ProductMapper::toResponse)
        .toList();
  }

  public ProductResponse findById(Long id) {
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Producto " + id + " no encontrado"));
    return ProductMapper.toResponse(product);
  }

  @Transactional
  public ProductResponse create(ProductRequest request) {
    Category category = categoryRepository.findById(request.categoryId())
        .orElseThrow(() -> new ResourceNotFoundException("Categoría " + request.categoryId() + " no encontrada"));
    Product product = new Product();
    Product savedProduct = productRepository.save(ProductMapper.toEntity(request, product, category));

    ProductCreatedEvent event = new ProductCreatedEvent(
        savedProduct.getId(),
        savedProduct.getName(),
        savedProduct.getDescription(),
        savedProduct.getPrice(),
        savedProduct.getCategory().getId()
    );
    productEventProducer.publishProductCreated(event);

    return ProductMapper.toResponse(savedProduct);
  }

  @Transactional
  public ProductResponse update(Long id, ProductRequest request) {
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Producto " + id + " no encontrado"));
    Category category = categoryRepository.findById(request.categoryId())
        .orElseThrow(() -> new ResourceNotFoundException("Categoría " + request.categoryId() + " no encontrada"));
    Product updated = productRepository.save(ProductMapper.toEntity(request, product, category));
    return ProductMapper.toResponse(updated);
  }

  @Transactional
  public void delete(Long id) {
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Producto " + id + " no encontrado"));
    productRepository.delete(product);
  }

  public List<ProductSummaryResponse> findAllAndSumary() {
    List<Product> products = productRepository.findAll();
    return products.stream()
        .map(ProductMapper::toSummaryResponse)
        .collect(Collectors.toList());
  }
}
