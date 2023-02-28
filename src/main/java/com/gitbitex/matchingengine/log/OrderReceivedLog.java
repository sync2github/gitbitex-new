package com.gitbitex.matchingengine.log;

import java.math.BigDecimal;
import java.util.Date;

import com.gitbitex.enums.OrderSide;
import com.gitbitex.enums.OrderType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderReceivedLog extends OrderLog {
    private String orderId;
    private String userId;
    private BigDecimal size;
    private BigDecimal price;
    private BigDecimal funds;
    private OrderSide side;
    private OrderType orderType;
    private String clientOid;
    private Date time;

    public OrderReceivedLog() {
        this.setType(LogType.ORDER_RECEIVED);
    }
}

