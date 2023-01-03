package me.potato.springaxon01simple.core.exceptions;

public class UnconfirmedOrderException extends RuntimeException {
    public UnconfirmedOrderException(String orderId) {
        super("Order " + orderId + " is not confirmed yet");
    }
}
