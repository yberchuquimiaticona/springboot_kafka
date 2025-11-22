package dev.ychuquimia.product_service.exception;

public class CategoryAlreadyExistsException extends RuntimeException {
  public CategoryAlreadyExistsException(String name) {
    super("La categoría " + name + " ya existe");
  }
}