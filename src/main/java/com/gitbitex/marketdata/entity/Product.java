package com.gitbitex.marketdata.entity;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Product {

    private Date createdAt;

    private Date updatedAt;

    private String productId;

    private String baseCurrency;

    private String quoteCurrency;

    private BigDecimal baseMinSize;

    private BigDecimal baseMaxSize;

    private BigDecimal quoteMinSize;

    private BigDecimal quoteMaxSize;

    private int baseScale;

    private int quoteScale;

    private float quoteIncrement;

    private float takerFeeRate;

    private float makerFeeRate;

    private int displayOrder;
}
