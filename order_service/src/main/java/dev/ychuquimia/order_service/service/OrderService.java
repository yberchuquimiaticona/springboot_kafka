package dev.ychuquimia.order_service.service;

import dev.ychuquimia.order_service.kafka.consumer.OrderEventConsumer;
import dev.ychuquimia.order_service.kafka.event.OrderPlacedEvent;
import dev.ychuquimia.order_service.kafka.producer.OrderEventProducer;
import dev.ychuquimia.order_service.model.dto.OrderRequest;
import dev.ychuquimia.order_service.model.dto.OrderResponse;
import dev.ychuquimia.order_service.model.entity.Order;
import dev.ychuquimia.order_service.model.entity.OrderStatus;
import dev.ychuquimia.order_service.repository.OrderRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

  private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);
  private final OrderRepository orderRepository;
  private final OrderEventProducer orderEventProducer; // Nuevo

  public OrderService(OrderRepository orderRepository,
      OrderEventProducer orderEventProducer) { // Nuevo
    this.orderRepository = orderRepository;
    this.orderEventProducer = orderEventProducer; // Nuevo
  }

  @Transactional
  public OrderResponse create(OrderRequest request) {
    // 1. Crear y guardar orden
    Order order = new Order();
    order.setProductId(request.getProductId());
    order.setQuantity(request.getQuantity());
    order.setCustomerName(request.getCustomerName());
    order.setCustomerEmail(request.getCustomerEmail());
    order.setTotalAmount(request.getTotalAmount());

    Order saved = orderRepository.save(order);

    // 2. Publicar evento a Kafka
    OrderPlacedEvent event = new OrderPlacedEvent(
        saved.getId(),
        saved.getProductId(),
        saved.getQuantity(),
        saved.getCustomerName(),
        saved.getCustomerEmail(),
        saved.getTotalAmount()
    );
    orderEventProducer.publishOrderPlaced(event);

    // 3. Retornar respuesta
    return mapToResponse(saved);
  }

  // Resto de métodos sin cambios...
  @Transactional(readOnly = true)
  public List<OrderResponse> findAll() {
    return orderRepository.findAll().stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public OrderResponse findById(Long id) {
    Order order = orderRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
    // Nota: En producción, crear OrderNotFoundException extends RuntimeException
    // Ver Clase 3 para manejo de excepciones con @ControllerAdvice
    return mapToResponse(order);
  }

  /**
   * Confirma una orden (actualiza estado de PENDING a CONFIRMED) Llamado cuando inventory-service
   * confirma que hay stock
   */
  @Transactional
  public void confirmOrder(Long orderId) {
    log.info("Confirming order: orderId={}", orderId);

    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

    // Verificar que la orden está en estado PENDING
    if (order.getStatus() != OrderStatus.PENDING) {
      log.warn("Order is not PENDING, cannot confirm: orderId={}, currentStatus={}",
          orderId, order.getStatus());
      return; // Idempotencia: Si ya fue procesada, no hacer nada
    }

    // Actualizar estado
    order.setStatus(OrderStatus.CONFIRMED);
    orderRepository.save(order);

    log.info("Order confirmed: orderId={}, newStatus={}", orderId, order.getStatus());
  }

  /**
   * Cancela una orden (actualiza estado de PENDING a CANCELLED) Llamado cuando inventory-service
   * rechaza la orden por falta de stock
   */
  @Transactional
  public void cancelOrder(Long orderId, String reason) {
    log.info("Cancelling order: orderId={}, reason={}", orderId, reason);

    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

    // Verificar que la orden está en estado PENDING
    if (order.getStatus() != OrderStatus.PENDING) {
      log.warn("Order is not PENDING, cannot cancel: orderId={}, currentStatus={}",
          orderId, order.getStatus());
      return; // Idempotencia: Si ya fue procesada, no hacer nada
    }

    // Actualizar estado y guardar razón de cancelación
    order.setStatus(OrderStatus.CANCELLED);
    order.setCancellationReason(reason);
    orderRepository.save(order);

    log.info("Order cancelled: orderId={}, newStatus={}, reason={}",
        orderId, order.getStatus(), reason);
  }

  private OrderResponse mapToResponse(Order order) {
    OrderResponse response = new OrderResponse();
    response.setId(order.getId());
    response.setProductId(order.getProductId());
    response.setQuantity(order.getQuantity());
    response.setCustomerName(order.getCustomerName());
    response.setCustomerEmail(order.getCustomerEmail());
    response.setTotalAmount(order.getTotalAmount());
    response.setStatus(order.getStatus());
    response.setCreatedAt(order.getCreatedAt());
    return response;
  }
}
