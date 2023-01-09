package me.potato.springaxon01simple.core.commands;

import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
public class DecrementProductCountCommand {
    @TargetAggregateIdentifier
    private final String orderId;
    private final String productId;
}
