package info.ralab.uxdf.instance;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public final class IdMaker {
    static final int RADIX = 32;
    static final int DIGITS = 12;
    static final String MAX_VALUE_STRING = "vvvvvvvvvvvv";
    static final String MIN_VALUE_STRING = "000000000000";
    static final long MAX_VALUE = Long.valueOf(MAX_VALUE_STRING, RADIX);
    static final long MIN_VALUE = 0L;


    /**
     * 分区创建者
     */
    private IdAreaMaker idAreaMaker;
    /**
     * 分区
     */
    private String area;
    /**
     * 序列
     */
    private AtomicLong sequence;
    /**
     * 临时序列
     */
    private AtomicLong tempSequence;

    /**
     * 初始化，确定分区取值
     */
    private IdMaker() {
        this.idAreaMaker = new SimpleIdAreaMaker();
        this.area = this.idAreaMaker.next();
        this.sequence = new AtomicLong();
        this.tempSequence = new AtomicLong();
    }

    private static IdMaker seed = new IdMaker();

    /**
     * 初始化ID生成器，设置分区生成器
     *
     * @param idAreaMaker 分区生成器
     */
    public static synchronized void init(final IdAreaMaker idAreaMaker) {
        // ID分区生成器不能为空
        if (idAreaMaker == null) {
            throw new IllegalArgumentException("Id area maker not null.");
        }
        // 替换ID分区生成器，并重新设置分区
        seed.idAreaMaker = idAreaMaker;
        seed.area = idAreaMaker.next();
    }

    /**
     * 获得下一个ID
     *
     * @return ID
     */
    public static synchronized String next() {
        long nextSequence = seed.sequence.getAndIncrement();
        // 序列是否超过最大值
        if (nextSequence >= MAX_VALUE) {
            seed.area = seed.idAreaMaker.next();
            seed.sequence.set(0L);
            nextSequence = seed.sequence.getAndIncrement();
        }

        String nextValue = seed.area + fillDigits(Long.toString(nextSequence, RADIX));
        String checkDigit = makeCheckDigit(nextValue);
        return checkDigit + nextValue;
    }

    /**
     * 获得一个临时ID
     * @return 临时ID
     */
    public static String temp() {
        return "t_" + seed.tempSequence.getAndIncrement();
    }

    /**
     * ID值补齐位数
     *
     * @param value ID值
     * @return 补齐位数后的ID值
     */
    static String fillDigits(String value) {
        if (value.length() < DIGITS) {
            char[] fills = new char[DIGITS - value.length()];
            Arrays.fill(fills, '0');
            value = new String(fills) + value;
        }

        return value;
    }

    private static String makeCheckDigit(final String nextValue) {
        if (StringUtils.isBlank(nextValue) || nextValue.length() != DIGITS + DIGITS) {
            return null;
        }

        int sum = 0;
        for (int i = 0; i < nextValue.length(); i++) {
            int num = Integer.parseInt(nextValue.substring(i, i + 1), RADIX);
            if (num >= RADIX || num < 0) {
                return null;
            }

            sum += num;
        }
        return Integer.toString(sum / nextValue.length(), RADIX);
    }

    public static boolean effective(final String id) {
        if (StringUtils.isBlank(id) || id.length() != DIGITS + DIGITS + 1) {
            return false;
        }

        try {
            final String checkDigit = id.substring(0, 1);
            return checkDigit.equals(makeCheckDigit(id.substring(1)));
        } catch (Exception e) {
            log.debug("check id error.", e);
            return false;
        }
    }
}
