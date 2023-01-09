package me.potato.springaxon01simple.queryhandler;

import lombok.RequiredArgsConstructor;
import me.potato.springaxon01simple.core.query.FindAllOrderedProductsQuery;
import me.potato.springaxon01simple.core.query.Order;
import me.potato.springaxon01simple.core.query.OrderStatus;
import me.potato.springaxon01simple.core.query.TotalProductsShippedQuery;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class OrderQueryHandler {

    private final OrderEventHandler orderEventHandler;

    @QueryHandler
    public List<Order> handle(FindAllOrderedProductsQuery query) {
        return orderEventHandler.getOrders().values().stream().toList();
    }

    @QueryHandler
    public Integer handle(TotalProductsShippedQuery query) {
        return orderEventHandler.getOrders().values().stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.SHIPPED)
                .mapToInt(order -> order.getProducts().getOrDefault(query.getProductId(), 0))
                .sum();
    }
}
