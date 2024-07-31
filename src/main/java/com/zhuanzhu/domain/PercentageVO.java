package com.zhuanzhu.domain;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Liwq
 */
@Slf4j
public class PercentageVO {

    private final Long total;
    private final Integer onePercentCount;
    private final String prefix;

    public PercentageVO(Long total, String prefix) {
        this.onePercentCount = Math.max((int) Math.ceil(total * 0.01), 1000);
        this.total = total;
        this.prefix = prefix;
    }

    public boolean enableShowRate(long index) {
        return index % onePercentCount == 0;
    }

    public void showRate(long index) {
        if (enableShowRate(index)) {
            log.info("===== {}：{}% =====", prefix, String.format("%.2f", (double) index / total * 100));
        }
    }
}
