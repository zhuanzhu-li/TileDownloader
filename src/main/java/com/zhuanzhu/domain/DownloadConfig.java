package com.zhuanzhu.domain;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author Liwq
 */
@Data
public class DownloadConfig {
    private Range range;

    private Level level;

    private String savePath;

    private String url;

    private String fileSuffix;

    private String authKeyName;

    private List<AuthKey> authKeys;

    private int[] weights;

    public static Geometry geometry;

    private Boolean boundSelectEnable;

    public static DownloadConfig loadConfig(String configFileAbsPath) throws IOException {
        Yaml yaml = new Yaml();
        File configFile = new File(configFileAbsPath);
        Map<String, Object> data = yaml.load(Files.newInputStream(configFile.toPath()));
        Object geom = data.get("geom");
        if(geom != null){
            try {
                geometry = new WKBReader().read(WKBReader.hexToBytes(String.valueOf(geom)));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        final String string = JSON.toJSONString(data);
        return JSON.parseObject(string, DownloadConfig.class);
    }

    public ReferencedEnvelope getWgsEnvelope() {
        return new ReferencedEnvelope(range.getMinLon(), range.getMaxLon()
                , range.getMinLat(), range.getMaxLat(), DefaultGeographicCRS.WGS84);
    }

    public String getKeyByWeight() {
        if (authKeys == null || authKeys.isEmpty()) {
            return "";
        }
        if (authKeys.size() == 1) {
            return authKeys.get(0).getKey();
        }
        if (weights == null) {
            weights = new int[authKeys.size()];
            for (int i = 0; i < authKeys.size(); i++) {
                weights[i] = authKeys.get(i).getWeight();
            }
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
