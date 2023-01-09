package me.potato.springaxon01simple.core.exceptions;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderAlreadyConfirmedException extends RuntimeException {
    private final String orderId;
}
