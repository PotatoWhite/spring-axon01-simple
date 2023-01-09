package me.potato.springaxon01simple.queryhandler;

import lombok.Data;
import me.potato.springaxon01simple.core.events.*;
import me.potato.springaxon01simple.core.query.Order;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Data
@Service
public class OrderEventHandler {

    // DB라고 가정하고 Map으로 구현
    private final Map<String, Order> orders = new HashMap<>();

    @EventHandler
    public void on(OrderCreatedEvent event) {
        orders.put(event.getOrderId(), new Order(event.getOrderId()));
    }

    @EventHandler
    public void on(OrderConfirmedEvent event) {
        orders.computeIfPresent(event.getOrderId(), (orderId, order) -> {
            order.setOrderConfirmed();
            return order;
        });
    }

    @EventHandler
    public void on(OrderShippedEvent event) {
        orders.computeIfPresent(event.getOrderId(), (orderId, order) -> {
            order.setOrderShipped();
            order.setAddress(event.getAddress());
            return order;
        });
    }

    // session 02 : ProductAddedEvent
    @EventHandler
    public void on(ProductAddedEvent event) {
        orders.computeIfPresent(event.getOrderId(), (orderId, order) -> {
            order.addProduct(event.getProductId());
            return order;
        });
    }

    @EventHandler
    public void on(ProductCountIncrementedEvent event) {
        orders.computeIfPresent(event.getOrderId(), (orderId, value) -> {
            value.increaseProduct(event.getProductId());
            return value;
        });
    }

    @EventHandler
    public void on(ProductCountDecrementedEvent event) {
        orders.computeIfPresent(event.getOrderId(), (orderId, value) -> {
            value.decreaseProduct(event.getProductId());
            return value;
        });
    }

    @EventHandler
    public void on(ProductRemovedEvent event) {
        orders.computeIfPresent(event.getOrderId(), (orderId, order) -> {
            order.removeProduct(event.getProductId());
            return order;
        });
    }
}
