package com.zhuanzhu.domain;

import lombok.Data;

import java.util.List;
import java.util.Random;

/**
 * @author Liwq
 */
@Data
public class AuthKey {

    private String key;

    private Integer weight;

    public static String getKeyByWeight(List<AuthKey> authKeys) {
        if (authKeys.size() == 1) {
            return authKeys.get(0).getKey();
        }
        int[] weights = new int[authKeys.size()];
        for (int i = 0; i < authKeys.size(); i++) {
            weights[i] = authKeys.get(i).getWeight();
        }
        return authKeys.get(getKeyIndex(weights)).getKey();
    }

    private static int getKeyIndex(int[] weights) {
        // 创建一个随机数生成器
        Random rand = new Random();

        // 计算权重总和
        int totalWeight = 0;
        for (int weight : weights) {
            totalWeight += weight;
        }

        // 生成随机数，然后根据权重分配概率
        int randomNum = rand.nextInt(totalWeight);
        int weightSum = 0;
        int keyIndex = 0;
        for (int i = 0; i < weights.length; i++) {
            weightSum += weights[i];
            if (randomNum < weightSum) {
                keyIndex = i;
                break;
            }
        }
        return keyIndex;
    }
}
