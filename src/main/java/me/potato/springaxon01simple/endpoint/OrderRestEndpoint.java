package me.potato.springaxon01simple.endpoint;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.potato.springaxon01simple.core.commands.ConfirmOrderCommand;
import me.potato.springaxon01simple.core.commands.CreateOrderCommand;
import me.potato.springaxon01simple.core.commands.ShipOrderCommand;
import me.potato.springaxon01simple.core.query.FindAllOrderedProductsQuery;
import me.potato.springaxon01simple.core.query.Order;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("orders")
public class OrderRestEndpoint {
    private final CommandGateway commandGateway;
    private final QueryGateway   queryGateway;

    @PostMapping("ship-order")
    public Mono shipOrder() {
        var orderId   = UUID.randomUUID().toString();
        var productId = UUID.randomUUID().toString();
        log.info("shipOrder: orderId={}, productId={}", orderId, productId);
        return Mono.fromFuture(commandGateway.send(new CreateOrderCommand(orderId, productId))
                .thenCompose(s -> commandGateway.send(new ConfirmOrderCommand(orderId)))
                .thenCompose(s -> commandGateway.send(new ShipOrderCommand(orderId))));
    }

    @GetMapping
    public Mono<List<Order>> getOrders() {
        return Mono.fromFuture(queryGateway.query(new FindAllOrderedProductsQuery(), ResponseTypes.multipleInstancesOf(Order.class)));
    }
}
