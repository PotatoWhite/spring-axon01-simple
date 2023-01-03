package me.potato.springaxon01simple.core.commands;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@RequiredArgsConstructor
public final class ShipOrderCommand {
    @TargetAggregateIdentifier
    private final String orderId;
}
