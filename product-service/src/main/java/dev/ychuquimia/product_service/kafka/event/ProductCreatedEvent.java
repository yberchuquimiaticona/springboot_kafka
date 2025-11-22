package dev.ychuquimia.product_service.kafka.event;

import java.math.BigDecimal;
import java.time.Instant;

public class ProductCreatedEvent {

  private Long productId;
  private String name;
  private String description;
  private BigDecimal price;
  private Long categoryId;
  private Instant timestamp;

  public ProductCreatedEvent() {
    // Constructor sin argumentos requerido para deserialización JSON
  }

  public ProductCreatedEvent(Long productId, String name, String description,
      BigDecimal price, Long categoryId) {
    this.productId = productId;
    this.name = name;
    this.description = description;
    this.price = price;
    this.categoryId = categoryId;
    this.timestamp = Instant.now();
  }

  // Getters y Setters
  public Long getProductId() {
    return productId;
  }

  public void setProductId(Long productId) {
    this.productId = productId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  public Long getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(Long categoryId) {
    this.categoryId = categoryId;
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Instant timestamp) {
    this.timestamp = timestamp;
  }

  @Override
  public String toString() {
    return "ProductCreatedEvent{" +
        "productId=" + productId +
        ", name='" + name + '\'' +
        ", price=" + price +
        ", timestamp=" + timestamp +
        '}';
  }
}