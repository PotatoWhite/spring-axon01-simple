package me.potato.springaxon01simple.commandmodel.order;

import lombok.extern.slf4j.Slf4j;
import me.potato.springaxon01simple.core.commands.AddProductCommand;
import me.potato.springaxon01simple.core.commands.ConfirmOrderCommand;
import me.potato.springaxon01simple.core.commands.CreateOrderCommand;
import me.potato.springaxon01simple.core.commands.ShipOrderCommand;
import me.potato.springaxon01simple.core.events.*;
import me.potato.springaxon01simple.core.exceptions.OrderAlreadyConfirmedException;
import me.potato.springaxon01simple.core.exceptions.UnconfirmedOrderException;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.modelling.command.AggregateMember;
import org.axonframework.spring.stereotype.Aggregate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Aggregate
public class OrderAggregate {
    @AggregateIdentifier
    private String  orderId;
    private String  address;
    private boolean orderConfirmed;

    @AggregateMember
    private Map<String, OrderLine> orderLines;

    protected OrderAggregate() {
    }

    @CommandHandler
    public OrderAggregate(CreateOrderCommand command) {
        AggregateLifecycle.apply(new OrderCreatedEvent(command.getOrderId()));
    }

    @EventSourcingHandler
    public void on(OrderCreatedEvent event) {
        this.orderId        = event.getOrderId();
        this.orderConfirmed = false;
        this.orderLines     = new HashMap<>();
    }

    @CommandHandler
    public void handle(ConfirmOrderCommand command) {
        if (orderConfirmed) return;

        AggregateLifecycle.apply(new OrderConfirmedEvent(command.getOrderId()));
    }

    @EventSourcingHandler
    public void on(OrderConfirmedEvent event) {
        this.orderConfirmed = true;
    }

    @CommandHandler
    public void handle(ShipOrderCommand command) {
        if (!orderConfirmed) throw new UnconfirmedOrderException(command.getOrderId());

        AggregateLifecycle.apply(new OrderShippedEvent(command.getOrderId(), command.getAddress()));
    }

    @EventSourcingHandler
    public void on(OrderShippedEvent event) {
        this.address = event.getAddress();
    }

    // Idempotent
    @CommandHandler
    public void handle(AddProductCommand command) {
        if (orderConfirmed)
            throw new OrderAlreadyConfirmedException(command.getOrderId());

        var productId = command.getProductId();
        if (orderLines.containsKey(productId)) {
            log.info("DuplicateOrderLineException: {}", command.getOrderId());
            AggregateLifecycle.apply(new ProductCountIncrementedEvent(command.getOrderId(), productId));
            return;
        }

        AggregateLifecycle.apply(new ProductAddedEvent(command.getOrderId(), command.getProductId()));
    }

    @EventSourcingHandler
    public void on(ProductAddedEvent event) {
        var productId = event.getProductId();
        orderLines.put(productId, new OrderLine(productId));
    }

    @EventSourcingHandler
    public void on(ProductRemovedEvent event) {
        orderLines.remove(event.getProductId());
    }
}
