package me.potato.springaxon01simple.core.events;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public final class OrderConfirmedEvent {
    private final String orderId;
}
