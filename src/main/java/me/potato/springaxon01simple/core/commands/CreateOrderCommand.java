package me.potato.springaxon01simple.core.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@RequiredArgsConstructor
public class CreateOrderCommand {
    @TargetAggregateIdentifier
    private final String orderId;
    private final String productId;
}