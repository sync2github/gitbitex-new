package com.gitbitex.matchingengine;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class Trade {
    private long tradeId;
    private BigDecimal size;
    private BigDecimal funds;
    private BigDecimal price;
}