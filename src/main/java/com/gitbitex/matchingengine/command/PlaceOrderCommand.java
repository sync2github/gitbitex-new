package com.gitbitex.matchingengine.command;

import com.gitbitex.marketdata.entity.Order;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlaceOrderCommand extends MatchingEngineCommand {
    private Order order;

    public PlaceOrderCommand() {
        this.setType(CommandType.PLACE_ORDER);
    }
}
