package me.potato.springaxon01simple.core.query;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class TotalProductsShippedQuery {
    private final String productId;
}
