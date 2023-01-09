package me.potato.springaxon01simple.core.commands;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@RequiredArgsConstructor
public class AddProductCommand {
    @TargetAggregateIdentifier
    private final String orderId;
    private final String productId;
}
