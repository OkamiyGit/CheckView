package com.test.okamiy.mycheckview.view;

/**
 * Created by Okamiy on 2017/10/26.
 * Email: 542839122@qq.com
 * 动画执行速度
 */

enum TickRateEnum {
    //低速
    SLOW(6, 4, 2),
    //正常
    NORMAL(12, 6, 4),
    //高速
    FAST(20, 14, 8);

    public static final int RATE_MODE_SLOW = 0;
    public static final int RATE_MODE_NORMAL = 1;
    public static final int RATE_MODE_FAST = 2;

    //圆环进度增加的单位（小于90）
    private int ringCounterUnit;
    //圆圈收缩的单位
    private int circleCounterUnit;
    //圆圈最后放大收缩的单位
    private int scaleCounterUnit;

    TickRateEnum(int ringCounterUnit, int circleCounterUnit, int scaleCounterUnit) {
        this.ringCounterUnit = ringCounterUnit;
        this.circleCounterUnit = circleCounterUnit;
        this.scaleCounterUnit = scaleCounterUnit;
    }

    public int getRingCounterUnit() {
        return ringCounterUnit;
    }

    public TickRateEnum setRingCounterUnit(int ringCounterUnit) {
        this.ringCounterUnit = ringCounterUnit;
        return this;
    }

    public int getCircleCounterUnit() {
        return circleCounterUnit;
    }

    public TickRateEnum setCircleCounterUnit(int circleCounterUnit) {
        this.circleCounterUnit = circleCounterUnit;
        return this;
    }

    public int getScaleCounterUnit() {
        return scaleCounterUnit;
    }

    public TickRateEnum setScaleCounterUnit(int scaleCounterUnit) {
        this.scaleCounterUnit = scaleCounterUnit;
        return this;
    }

    public static TickRateEnum getRateEnum(int rateMode) {
        TickRateEnum tickRateEnum;
        switch (rateMode) {
            case RATE_MODE_SLOW:
                tickRateEnum = TickRateEnum.SLOW;
                break;
            case RATE_MODE_NORMAL:
                tickRateEnum = TickRateEnum.NORMAL;
                break;
            case RATE_MODE_FAST:
                tickRateEnum = TickRateEnum.FAST;
                break;
            default:
                tickRateEnum = TickRateEnum.NORMAL;
                break;
        }
        return tickRateEnum;
    }
}
