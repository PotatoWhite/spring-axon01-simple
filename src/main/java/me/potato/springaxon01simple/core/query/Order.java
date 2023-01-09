package me.potato.springaxon01simple.core.query;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@RequiredArgsConstructor
public class Order {
    private final Map<String, Integer> products = new HashMap<>();

    private final String      orderId;
    private       String      address;
    private       OrderStatus orderStatus = OrderStatus.CREATED;


    public void setOrderConfirmed() {
        this.orderStatus = OrderStatus.CONFIRMED;
    }

    public void setOrderShipped() {
        this.orderStatus = OrderStatus.SHIPPED;
    }

    public void addProduct(String product) {
        products.putIfAbsent(product, 1);
    }

    public void increaseProduct(String product) {
        products.computeIfPresent(product, (key, value) -> ++value);
    }

    public void decreaseProduct(String product) {
        products.computeIfPresent(product, (key, value) -> --value);
    }

    public void removeProduct(String product) {
        products.remove(product);
    }
}
