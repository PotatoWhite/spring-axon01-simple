package me.potato.springaxon01simple.core.events;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class ProductAddedEvent {
    private final String orderId;
    private final String productId;
}
