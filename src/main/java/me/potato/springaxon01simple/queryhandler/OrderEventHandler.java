package me.potato.springaxon01simple.queryhandler;

import me.potato.springaxon01simple.core.events.OrderConfirmedEvent;
import me.potato.springaxon01simple.core.events.OrderCreatedEvent;
import me.potato.springaxon01simple.core.events.OrderShippedEvent;
import me.potato.springaxon01simple.core.query.FindAllOrderedProductsQuery;
import me.potato.springaxon01simple.core.query.Order;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderEventHandler {

    // DB라고 가정하고 Map으로 구현
    private final Map<String, Order> orders = new HashMap<>();

    @EventHandler
    public void on(OrderCreatedEvent event) {
        orders.put(event.getOrderId(), new Order(event.getOrderId(), event.getProductId()));
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
            return order;
        });
    }

    @QueryHandler
    public List<Order> handle(FindAllOrderedProductsQuery query) {
        return orders.values().stream().toList();
    }
}
