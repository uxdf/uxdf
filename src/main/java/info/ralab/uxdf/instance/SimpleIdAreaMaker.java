package info.ralab.uxdf.instance;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 简单ID分区生成器，基于内存计数
 */
public class SimpleIdAreaMaker implements IdAreaMaker {

    private AtomicLong area = new AtomicLong();

    @Override
    public String next() {
        return IdMaker.fillDigits(Long.toString(area.getAndIncrement(), IdMaker.RADIX));
    }
}
