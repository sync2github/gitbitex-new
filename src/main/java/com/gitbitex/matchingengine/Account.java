package com.gitbitex.matchingengine;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Account implements Cloneable {
    private String userId;
    private String currency;
    private BigDecimal available;
    private BigDecimal hold;

    @Override
    public Account clone() {
        try {
            return (Account)super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
