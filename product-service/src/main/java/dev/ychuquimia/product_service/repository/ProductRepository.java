package dev.ychuquimia.product_service.repository;

import dev.ychuquimia.product_service.model.Product;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

  @EntityGraph(attributePaths = "category")
  @Override
  List<Product> findAll();

  @EntityGraph(attributePaths = "category")
  List<Product> findByNameContainingIgnoreCase(String name);

  @EntityGraph(attributePaths = "category")
  List<Product> findByCategoryId(Long categoryId);
}
