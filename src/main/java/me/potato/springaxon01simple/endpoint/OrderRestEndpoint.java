package me.potato.springaxon01simple.endpoint;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.potato.springaxon01simple.core.commands.ConfirmOrderCommand;
import me.potato.springaxon01simple.core.commands.CreateOrderCommand;
import me.potato.springaxon01simple.core.commands.ShipOrderCommand;
import me.potato.springaxon01simple.core.query.FindAllOrderedProductsQuery;
import me.potato.springaxon01simple.core.query.Order;
import me.potato.springaxon01simple.endpoint.dto.CreateOrderCommandRequest;
import org.axonframework.extensions.reactor.commandhandling.gateway.ReactorCommandGateway;
import org.axonframework.extensions.reactor.queryhandling.gateway.ReactorQueryGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("orders")
public class OrderRestEndpoint {
    private final ReactorCommandGateway commandGateway;
    private final ReactorQueryGateway   queryGateway;

    @PostMapping("ship-order")
    public Mono<Object> shipOrder(@RequestBody CreateOrderCommandRequest request) {
        log.info("Received request to ship order: {}", request);

        // create new OrderId and send command to ship order
        var orderId            = UUID.randomUUID().toString();
        var createOrderCommand = new CreateOrderCommand(orderId, request.productId());

        return commandGateway.send(createOrderCommand)
                .then(commandGateway.send(new ConfirmOrderCommand(orderId)))
                .then(commandGateway.send(new ShipOrderCommand(orderId, request.address())));
    }

    @GetMapping
    public Mono<List<Order>> getOrders() {
        return queryGateway.query(new FindAllOrderedProductsQuery(), ResponseTypes.multipleInstancesOf(Order.class));
    }
}
