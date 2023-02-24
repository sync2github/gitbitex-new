package com.gitbitex.matchingengine.snapshot;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.alibaba.fastjson.JSON;

import com.gitbitex.matchingengine.EngineSnapshot;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderBookManager {
    private final RedissonClient redissonClient;
    private final RTopic l2BatchNotifyTopic;

    public OrderBookManager(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
        this.l2BatchNotifyTopic = redissonClient.getTopic("l2_batch", StringCodec.INSTANCE);
    }

    @SneakyThrows
    public EngineSnapshot getFullOrderBookSnapshot() {
        Path path = Paths.get("matching-engine-snapshot.log");
        if (!Files.exists(path)) {
            return null;
        }
        byte[] bytes = Files.readAllBytes(path);
        return JSON.parseObject(new String(bytes), EngineSnapshot.class);
    }

    @SneakyThrows
    public void saveFullOrderBookSnapshot(EngineSnapshot snapshot) {
        Path path = Paths.get("matching-engine-snapshot.log");
        if (Files.exists(path)) {
            Files.delete(path);
        }
        Files.write(path,
            JSON.toJSONString(snapshot, true).getBytes(StandardCharsets.UTF_8));
    }

    public void saveL3OrderBook(L3OrderBook l3OrderBook) {
        redissonClient.getBucket(keyForL3(l3OrderBook.getProductId()), StringCodec.INSTANCE).set(
            JSON.toJSONString(l3OrderBook));
    }

    public L3OrderBook getL3OrderBook(String productId) {
        Object o = redissonClient.getBucket(keyForL3(productId), StringCodec.INSTANCE).get();
        if (o == null) {
            return null;
        }
        return JSON.parseObject(o.toString(), L3OrderBook.class);
    }

    public void saveL2OrderBook(L2OrderBook l2OrderBook) {
        redissonClient.getBucket(keyForL2(l2OrderBook.getProductId()), StringCodec.INSTANCE).setAsync(
            JSON.toJSONString(l2OrderBook));
    }

    public L2OrderBook getL2OrderBook(String productId) {
        Object o = redissonClient.getBucket(keyForL2(productId), StringCodec.INSTANCE).get();
        if (o == null) {
            return null;
        }
        return JSON.parseObject(o.toString(), L2OrderBook.class);
    }

    public void saveL2BatchOrderBook(L2OrderBook l2OrderBook) {
        redissonClient.getBucket(keyForL2Batch(l2OrderBook.getProductId()), StringCodec.INSTANCE)
            .setAsync(JSON.toJSONString(l2OrderBook))
            .onComplete((unused, throwable) -> {
                l2BatchNotifyTopic.publishAsync(l2OrderBook.getProductId());
            });
    }

    public L2OrderBook getL2BatchOrderBook(String productId) {
        Object o = redissonClient.getBucket(keyForL2Batch(productId), StringCodec.INSTANCE).get();
        if (o == null) {
            return null;
        }
        return JSON.parseObject(o.toString(), L2OrderBook.class);
    }

    public L2OrderBook getL1OrderBook(String productId) {
        Object o = redissonClient.getBucket(keyForL1(productId), StringCodec.INSTANCE).get();
        if (o == null) {
            return null;
        }
        return JSON.parseObject(o.toString(), L2OrderBook.class);
    }

    public void saveL1OrderBook(L2OrderBook orderBook) {
        redissonClient.getBucket(keyForL1(orderBook.getProductId()), StringCodec.INSTANCE).set(
            JSON.toJSONString(orderBook));
    }

    private String keyForL1(String productId) {
        return productId + ".l1_order_book";
    }

    private String keyForL2(String productId) {
        return productId + ".l2_order_book";
    }

    private String keyForL2Batch(String productId) {
        return productId + ".l2_batch_order_book";
    }

    private String keyForL3(String productId) {
        return productId + ".l3_order_book";
    }

    private String keyForFull(String productId) {
        return productId + ".full_order_book";
    }
}
