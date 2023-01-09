package me.potato.springaxon01simple.endpoint;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.potato.springaxon01simple.core.commands.*;
import me.potato.springaxon01simple.endpoint.dto.CreateOrderCommandRequest;
import me.potato.springaxon01simple.endpoint.dto.ShipOrderCommandRequest;
import org.axonframework.extensions.reactor.commandhandling.gateway.ReactorCommandGateway;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("command/order")
public class OrderCommandEndpoint {
    private final ReactorCommandGateway commandGateway;

    @PostMapping("ship-order")
    public Mono<Object> shipOrder(@RequestBody CreateOrderCommandRequest request) {
        log.info("Received request to ship order: {}", request);

        // create new OrderId and send command to ship order
        var orderId            = UUID.randomUUID().toString();
        var createOrderCommand = new CreateOrderCommand(orderId);

        // saga
        return commandGateway.send(createOrderCommand)
                .then(commandGateway.send(new AddProductCommand(orderId, request.productId())))
                .then(commandGateway.send(new ConfirmOrderCommand(orderId)))
                .then(commandGateway.send(new ShipOrderCommand(orderId, request.address())));
    }


    // session 01
    @PostMapping("ship-unconfirmed-order")
    public Mono<Object> shipUnconfirmedOrder(@RequestBody CreateOrderCommandRequest request) {
        log.info("Received request to ship unconfirmed order: {}", request);

        // create new OrderId and send command to ship order
        var orderId            = UUID.randomUUID().toString();
        var createOrderCommand = new CreateOrderCommand(orderId);

        // saga
        return commandGateway.send(createOrderCommand)
                .then(commandGateway.send(new AddProductCommand(orderId, request.productId())))
                // This throws an exception because the order is not confirmed yet.
                .then(commandGateway.send(new ShipOrderCommand(orderId, request.address())));
    }

    // session 02 : order is created but not confirmed
    @PostMapping
    public Mono<Object> createOrder() {
        log.info("Received request to order");

        return createOrder(UUID.randomUUID().toString());
    }

    // session 02 : order with existing orderId
    @PostMapping("{orderId}")
    public Mono<Object> createOrder(@PathVariable String orderId) {
        log.info("Received request to create order: {}", orderId);

        var createOrderCommand = new CreateOrderCommand(orderId);
        return commandGateway.send(createOrderCommand);
    }

    // session 02 : add product to cart
    @PostMapping("{orderId}/product/{productId}")
    public Mono<Object> addProduct(@PathVariable String orderId, @PathVariable String productId) {
        log.info("Received request to add product: {} to order: {}", productId, orderId);

        var addProductCommand = new AddProductCommand(orderId, productId);
        return commandGateway.send(addProductCommand);
    }

    // session 02 : increase quantity of product in cart
    @PostMapping("{orderId}/product/{productId}/increment")
    public Mono<Object> incrementProduct(@PathVariable String orderId, @PathVariable String productId) {
        log.info("Received request to increment product: {} to order: {}", productId, orderId);

        var incrementProductCountCommand = new IncrementProductCountCommand(orderId, productId);
        return commandGateway.send(incrementProductCountCommand);
    }

    // session 02 : decrease quantity of product in cart
    @PostMapping("{orderId}/product/{productId}/decrement")
    public Mono<Object> decrementProduct(@PathVariable String orderId, @PathVariable String productId) {
        log.info("Received request to decrement product: {} to order: {}", productId, orderId);

        var decrementProductCountCommand = new DecrementProductCountCommand(orderId, productId);
        return commandGateway.send(decrementProductCountCommand);
    }

    // session 02 : confirm order
    @PostMapping("{orderId}/confirm")
    public Mono<Object> confirmOrder(@PathVariable String orderId) {
        log.info("Received request to confirm order: {}", orderId);

        var confirmOrderCommand = new ConfirmOrderCommand(orderId);
        return commandGateway.send(confirmOrderCommand);
    }

    // session 02 : ship order
    @PostMapping("{orderId}/ship")
    public Mono<Object> shipOrder(@PathVariable String orderId, @RequestBody ShipOrderCommandRequest request) {
        log.info("Received request to ship order: {} to address: {}", orderId, request);

        var shipOrderCommand = new ShipOrderCommand(orderId, request.address());
        return commandGateway.send(shipOrderCommand);
    }
}
