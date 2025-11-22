package dev.ychuquimia.product_service.controller;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kafka-test")
public class KafkaTestController {

  private final KafkaTemplate<String, String> kafkaTemplate;

  public KafkaTestController(KafkaTemplate<String, String> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  @GetMapping("/test")
  public String test() {
    return "KafkaTemplate inyectado correctamente: " +
        kafkaTemplate.getClass().getName();
  }
}