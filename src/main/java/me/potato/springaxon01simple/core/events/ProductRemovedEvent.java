package me.potato.springaxon01simple.core.events;

import lombok.Data;

@Data
public class ProductRemovedEvent {
    private final String orderId;
    private final String productId;
}
