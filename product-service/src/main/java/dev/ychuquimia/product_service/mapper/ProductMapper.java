package dev.ychuquimia.product_service.mapper;


import dev.ychuquimia.product_service.dto.ProductRequest;
import dev.ychuquimia.product_service.dto.ProductResponse;
import dev.ychuquimia.product_service.dto.ProductSummaryResponse;
import dev.ychuquimia.product_service.model.Category;
import dev.ychuquimia.product_service.model.Product;

public final class ProductMapper {

  private ProductMapper() {
    throw new AssertionError("Utility class, no debe instanciarse");
  }

  public static ProductResponse toResponse(Product product) {
    Category category = product.getCategory();
    return new ProductResponse(
        product.getId(),
        product.getName(),
        product.getDescription(),
        product.getPrice(),
        product.getStock(),
        product.getCreatedAt(),
        product.getUpdatedAt(),
        category != null ? category.getId() : null,
        category != null ? category.getName() : null
    );
  }

  public static Product toEntity(ProductRequest request, Product entity, Category category) {
    entity.setName(request.name());
    entity.setDescription(request.description());
    entity.setPrice(request.price());
    entity.setStock(request.stock());
    entity.setCategory(category);
    return entity;
  }

  public static ProductSummaryResponse toSummaryResponse(Product product) {
    return new ProductSummaryResponse(
        product.getId(),
        product.getName(),
        product.getPrice()
    );
  }
}
