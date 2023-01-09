package me.potato.springaxon01simple.core.events;

import lombok.Data;

@Data
public class ProductCountDecrementedEvent {
    private final String orderId;
    private final String productId;
}
