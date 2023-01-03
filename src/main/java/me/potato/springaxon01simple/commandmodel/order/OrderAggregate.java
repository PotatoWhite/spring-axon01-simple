package me.potato.springaxon01simple.commandmodel.order;

import me.potato.springaxon01simple.core.commands.ConfirmOrderCommand;
import me.potato.springaxon01simple.core.commands.CreateOrderCommand;
import me.potato.springaxon01simple.core.commands.ShipOrderCommand;
import me.potato.springaxon01simple.core.events.OrderConfirmedEvent;
import me.potato.springaxon01simple.core.events.OrderCreatedEvent;
import me.potato.springaxon01simple.core.events.OrderShippedEvent;
import me.potato.springaxon01simple.core.exceptions.UnconfirmedOrderException;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

@Aggregate
public class OrderAggregate {
    @AggregateIdentifier
    private String  orderId;
    private boolean orderConfirmed;

    protected OrderAggregate() {
    }

    @CommandHandler
    public OrderAggregate(CreateOrderCommand command) {
        AggregateLifecycle.apply(new OrderCreatedEvent(command.getOrderId(), command.getProductId()));
    }

    @EventSourcingHandler
    public void on(OrderCreatedEvent event) {
        this.orderId        = event.getOrderId();
        this.orderConfirmed = false;
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

        AggregateLifecycle.apply(new OrderShippedEvent(command.getOrderId()));
    }
}
