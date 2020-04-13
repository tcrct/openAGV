package com.robot.adapter.exchange;

import static org.opentcs.util.Assertions.checkArgument;

/**
 * 以其初始值为界，以其最大值为界的计数器。
 * 如果计数器超过最大值，它将重置为初始值。
 *
 * @author Laotang
 * @since 1.0
 */
public class BoundedCounter {

    /**
     * 最大值
     */
    public static final int INT_MAX_VALUE = Integer.MAX_VALUE;
    /**
     * 初始化值
     */
    private final int initialValue;
    /**
     * 最大值
     */
    private final int maxValue;
    /**
     * 统计值
     */
    private int counterValue;

    /**
     * 构造函数
     *
     * @param initialValue 初始化值
     * @param maxValue     最大值
     */
    public BoundedCounter(int initialValue, int maxValue) {
        checkArgument(initialValue < maxValue,
                "initialValue has to lower than maxValue: %d < %d",
                initialValue,
                maxValue);
        this.initialValue = initialValue;
        this.maxValue = maxValue;
        this.counterValue = initialValue;
    }

    /**
     * 统计数字累计加1
     *
     * @return 当前统计数字
     */
    public int getAndIncrement() {
        int currValue = counterValue;
        counterValue++;
        if (counterValue > maxValue) {
            counterValue = initialValue;
        }
        return currValue;
    }

    /**
     * 取统计数字
     *
     * @return
     */
    public int getCounterValue() {
        return counterValue;
    }
}
