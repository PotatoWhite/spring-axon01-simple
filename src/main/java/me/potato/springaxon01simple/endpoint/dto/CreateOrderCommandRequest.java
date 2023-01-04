package me.potato.springaxon01simple.endpoint.dto;

public record CreateOrderCommandRequest(String productId, String address) {
}
