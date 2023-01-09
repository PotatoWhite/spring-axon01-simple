package me.potato.springaxon01simple.commandmodel.order;

import me.potato.springaxon01simple.core.commands.DecrementProductCountCommand;
import me.potato.springaxon01simple.core.commands.IncrementProductCountCommand;
import me.potato.springaxon01simple.core.events.OrderConfirmedEvent;
import me.potato.springaxon01simple.core.events.ProductCountDecrementedEvent;
import me.potato.springaxon01simple.core.events.ProductCountIncrementedEvent;
import me.potato.springaxon01simple.core.events.ProductRemovedEvent;
import me.potato.springaxon01simple.core.exceptions.OrderAlreadyConfirmedException;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.modelling.command.EntityId;

// order line is specific the details of the order
public class OrderLine {
    @EntityId
    private final String  productId;
    private       int     count;
    private       boolean orderConfirmed;

    public OrderLine(String productId) {
        this.productId      = productId;
        this.count          = 1;
        this.orderConfirmed = false;
    }

    @CommandHandler
    public void handle(IncrementProductCountCommand command) {
        if (orderConfirmed)
            throw new OrderAlreadyConfirmedException(command.getOrderId());

        AggregateLifecycle.apply(new ProductCountIncrementedEvent(command.getOrderId(), command.getProductId()));
    }

    @CommandHandler
    public void handle(DecrementProductCountCommand command) {
        if (orderConfirmed)
            throw new OrderAlreadyConfirmedException(command.getOrderId());

        if (count < 1) {
            AggregateLifecycle.apply(new ProductRemovedEvent(command.getOrderId(), command.getProductId()));
            return;
        }

        AggregateLifecycle.apply(new ProductCountDecrementedEvent(command.getOrderId(), command.getProductId()));
    }

    @EventSourcingHandler
    public void on(ProductCountIncrementedEvent event) {
        this.count++;
    }

    @EventSourcingHandler
    public void on(ProductCountDecrementedEvent event) {
        this.count--;
    }

    @EventSourcingHandler
    public void on(OrderConfirmedEvent event) {
        this.orderConfirmed = true;
    }
}
