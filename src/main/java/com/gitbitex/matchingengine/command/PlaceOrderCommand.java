package com.gitbitex.matchingengine.command;

import com.gitbitex.marketdata.entity.Order;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.procedure.spi.ParameterRegistrationImplementor;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
public class PlaceOrderCommand extends MatchingEngineCommand {
    private String productId;
    private String orderId;
    private String userId;
    private BigDecimal size;
    private BigDecimal price;
    private BigDecimal funds;
    private Order.OrderType orderType;
    private Order.OrderSide orderSide;
    private String baseCurrency;
    private String quoteCurrency;
    private Date time;

    public PlaceOrderCommand() {
        this.setType(CommandType.PLACE_ORDER);
    }
}
