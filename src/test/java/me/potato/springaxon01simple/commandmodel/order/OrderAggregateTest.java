package me.potato.springaxon01simple.commandmodel.order;

import me.potato.springaxon01simple.core.commands.CreateOrderCommand;
import me.potato.springaxon01simple.core.commands.ShipOrderCommand;
import me.potato.springaxon01simple.core.events.OrderConfirmedEvent;
import me.potato.springaxon01simple.core.events.OrderCreatedEvent;
import me.potato.springaxon01simple.core.events.OrderShippedEvent;
import me.potato.springaxon01simple.core.exceptions.UnconfirmedOrderException;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class OrderAggregateTest {
    private FixtureConfiguration<OrderAggregate> fixture;

    private static final String ORDER_ID   = UUID.randomUUID().toString();
    private static final String PRODUCT_ID = UUID.randomUUID().toString();

    @BeforeEach
    public void setUp() {
        fixture = new AggregateTestFixture<>(OrderAggregate.class);
    }

    @Test
    public void testCreateOrder_success() {
        fixture.givenNoPriorActivity()
                .when(new CreateOrderCommand(ORDER_ID, PRODUCT_ID))
                .expectEvents(new OrderCreatedEvent(ORDER_ID, PRODUCT_ID));
    }

    @Test
    public void ConfirmOrderCommand_success() {
        fixture.given(new OrderCreatedEvent(ORDER_ID, PRODUCT_ID), new OrderConfirmedEvent(ORDER_ID))
                .when(new ShipOrderCommand(ORDER_ID))
                .expectEvents(new OrderShippedEvent(ORDER_ID));
    }

    @Test
    public void ConfirmOrderCommand_fail() {
        fixture.given(new OrderCreatedEvent(ORDER_ID, PRODUCT_ID))
                .when(new ShipOrderCommand(ORDER_ID))
                .expectException(UnconfirmedOrderException.class);
    }
}