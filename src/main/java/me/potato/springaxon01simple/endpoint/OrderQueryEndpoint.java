package me.potato.springaxon01simple.endpoint;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.potato.springaxon01simple.core.query.FindAllOrderedProductsQuery;
import me.potato.springaxon01simple.core.query.Order;
import me.potato.springaxon01simple.core.query.TotalProductsShippedQuery;
import org.axonframework.extensions.reactor.queryhandling.gateway.ReactorQueryGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("query/order")
public class OrderQueryEndpoint {
    private final ReactorQueryGateway queryGateway;

    @GetMapping("all-orders")
    public Mono<List<Order>> getOrders() {
        return queryGateway.query(new FindAllOrderedProductsQuery(), ResponseTypes.multipleInstancesOf(Order.class));
    }

    @GetMapping("total-shipped/{productId}")
    public Mono<Integer> getTotalShipped(@PathVariable String productId) {
        return queryGateway.query(new TotalProductsShippedQuery(productId), Integer.class);
    }
}
