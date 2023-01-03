package me.potato.springaxon01simple.core.events;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public final class OrderCreatedEvent {
    private final String orderId;
    private final String productId;
}
