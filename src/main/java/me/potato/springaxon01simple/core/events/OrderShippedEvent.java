package me.potato.springaxon01simple.core.events;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public final class OrderShippedEvent {
    private final String orderId;

}
