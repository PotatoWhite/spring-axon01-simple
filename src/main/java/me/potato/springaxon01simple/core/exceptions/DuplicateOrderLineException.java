package me.potato.springaxon01simple.core.exceptions;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DuplicateOrderLineException extends RuntimeException {
    private final String orderId;
    private final String productId;
}
