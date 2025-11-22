package dev.ychuquimia.product_service.kafka.producer;

import dev.ychuquimia.product_service.kafka.event.ProductCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ProductEventProducer {

  private static final Logger log = LoggerFactory.getLogger(ProductEventProducer.class);
  private static final String TOPIC = "ecommerce.products.created";

  private final KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;

  public ProductEventProducer(KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public void publishProductCreated(ProductCreatedEvent event) {
    String key = event.getProductId().toString();

    log.info("Publishing ProductCreatedEvent: productId={}, name={}",
        event.getProductId(), event.getName());

    kafkaTemplate.send(TOPIC, key, event)
        .whenComplete((result, ex) -> {
          if (ex != null) {
            log.error("Failed to publish ProductCreatedEvent: productId={}",
                event.getProductId(), ex);
          } else {
            log.info("ProductCreatedEvent published successfully: productId={}, partition={}",
                event.getProductId(),
                result.getRecordMetadata().partition());
          }
        });
  }
}