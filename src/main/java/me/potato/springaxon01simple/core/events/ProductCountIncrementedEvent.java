package me.potato.springaxon01simple.core.events;

import lombok.Data;

@Data
public class ProductCountIncrementedEvent {
    private final String orderId;
    private final String productId;
}
