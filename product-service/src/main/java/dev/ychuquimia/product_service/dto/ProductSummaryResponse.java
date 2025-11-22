package dev.ychuquimia.product_service.dto;

import java.math.BigDecimal;


public record ProductSummaryResponse(
    Long id,
    String name,
    BigDecimal price
) {

}
