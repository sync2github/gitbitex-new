package com.gitbitex.order;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.alibaba.fastjson.JSON;

import com.gitbitex.AppProperties;
import com.gitbitex.marketdata.entity.Trade;
import com.gitbitex.marketdata.repository.TradeRepository;
import com.gitbitex.matchingengine.log.AccountChangeMessage;
import com.gitbitex.matchingengine.log.Log;
import com.gitbitex.matchingengine.log.LogDispatcher;
import com.gitbitex.matchingengine.log.LogHandler;
import com.gitbitex.matchingengine.log.OrderDoneMessage;
import com.gitbitex.matchingengine.log.OrderFilledMessage;
import com.gitbitex.matchingengine.log.OrderMatchLog;
import com.gitbitex.matchingengine.log.OrderOpenMessage;
import com.gitbitex.matchingengine.log.OrderReceivedMessage;
import com.gitbitex.matchingengine.log.OrderRejectedMessage;
import com.gitbitex.support.kafka.KafkaConsumerThread;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

@Slf4j
public class TradePersistenceThread extends KafkaConsumerThread<String, Log>
    implements ConsumerRebalanceListener, LogHandler {
    private final List<String> productIds;
    private final TradeRepository tradeRepository;
    private final AppProperties appProperties;
    private long uncommittedRecordCount;

    public TradePersistenceThread(List<String> productIds, TradeRepository tradeRepository,
        KafkaConsumer<String, Log> consumer, AppProperties appProperties) {
        super(consumer, logger);
        this.productIds = productIds;
        this.tradeRepository = tradeRepository;
        this.appProperties = appProperties;
    }

    @Override
    public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
        for (TopicPartition partition : partitions) {
            logger.info("partition revoked: {}", partition.toString());
        }
        consumer.commitSync();
    }

    @Override
    public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
        for (TopicPartition partition : partitions) {
            logger.info("partition assigned: {}", partition.toString());
        }
    }

    @Override
    protected void doSubscribe() {
        consumer.subscribe(Collections.singletonList(appProperties.getOrderBookLogTopic()), this);
    }

    @Override
    protected void doPoll() {
        var records = consumer.poll(Duration.ofSeconds(5));
        uncommittedRecordCount += records.count();

        for (ConsumerRecord<String, Log> record : records) {
            Log log = record.value();
            log.setOffset(record.offset());
            logger.info("{}", JSON.toJSONString(log));
            LogDispatcher.dispatch(log, this);
        }

        if (uncommittedRecordCount > 10) {
            consumer.commitSync();
            uncommittedRecordCount = 0;
        }
    }

    @Override
    public void on(OrderRejectedMessage log) {

    }

    @Override
    public void on(OrderReceivedMessage log) {

    }

    @Override
    public void on(OrderOpenMessage log) {

    }

    @Override
    public void on(OrderMatchLog log) {
        Trade trade = tradeRepository.findByProductIdAndTradeId(log.getProductId(),
            log.getTradeId());
        if (trade == null) {
            trade = new Trade();
            trade.setTradeId(log.getTradeId());
            trade.setTime(log.getTime());
            trade.setSize(log.getSize());
            trade.setPrice(log.getPrice());
            trade.setProductId(log.getProductId());
            trade.setMakerOrderId(log.getMakerOrderId());
            trade.setTakerOrderId(log.getTakerOrderId());
            trade.setSide(log.getSide());
            trade.setSequence(log.getSequence());
            trade.setOrderBookLogOffset(log.getOffset());
            tradeRepository.save(trade);
        }
    }

    @Override
    public void on(OrderDoneMessage log) {

    }

    @Override
    public void on(OrderFilledMessage log) {

    }

    @Override
    public void on(AccountChangeMessage log) {

    }
}