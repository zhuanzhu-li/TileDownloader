package test.test;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadPoolExecutor;

import com.google.common.collect.Lists;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import test.test.OwsUtils;
import test.test.TileGrid;

public class TileDownloader {
    private static final Logger log = LoggerFactory.getLogger(test.test.TileDownloader.class);

    ThreadPoolExecutor poolExecutor = null;

    public void amapDownload(String sourceUrl, int start, int end, String downloadPath, String fileName, ReferencedEnvelope wgsEnvelope, ThreadPoolTaskExecutor fixedThreadPool) throws Exception {
        CoordinateReferenceSystem crs = CRS.decode("EPSG:3857");
        ReferencedEnvelope envelope = wgsEnvelope.transform(crs, false);
        for (int z = start; z <= end; z++) {
            log.info("{}", Integer.valueOf(z));
            LinkedList<TileGrid> tileGrids = TileGrid.getTilesFromZ(envelope, z);
            log.info("z:{},{}", Integer.valueOf(z), Integer.valueOf(tileGrids.size()));
            File file = new File(downloadPath.replace("{z}", z + ""));
            Map<String, Integer> fileContains = null;
            log.info("");
            boolean isExists = false;
            if (fileContains != null) {
                log.info("{}", Integer.valueOf(fileContains.size()));
                isExists = true;
            }
            Map<String, Integer> finalFileContains = fileContains;
            boolean finalIsExists = isExists;
            int count = 0;
            int count2 = 0;
            for (TileGrid tileGrid : tileGrids) {
                long x = tileGrid.x;
                long y = Math.abs(tileGrid.y + 1L);
                long zz = tileGrid.z;
                String sourceUrl2 = sourceUrl.replace("{z}", String.valueOf(zz));
                sourceUrl2 = sourceUrl2.replace("{x}", String.valueOf(x));
                sourceUrl2 = sourceUrl2.replace("{y}", String.valueOf(y));
                String bashPath2 = downloadPath.replace("{z}", String.valueOf(zz));
                bashPath2 = bashPath2.replace("{x}", String.valueOf(x));
                bashPath2 = bashPath2.replace("{y}", String.valueOf(y));
                String fileName2 = fileName.replace("{z}", String.valueOf(zz));
                fileName2 = fileName2.replace("{x}", String.valueOf(x));
                fileName2 = fileName2.replace("{y}", String.valueOf(y));
                if ((new File(bashPath2 + "/" + fileName2)).exists()) {
                    count2++;
                    if (count2 % 10000 == 0) {
                        log.info("z:{},Download Count: {}", Long.valueOf(tileGrid.z), Integer.valueOf(count2));
                    }
                    continue;
                }
                int countTmp = ++count;

                String[] keys = {"4FA3YUrnk4ufid6LY7kX","nnPir3cnsIk5yzorthF7","FrEn078v226J4kM6MT7o"};
                int keyIndex = getKeyIndex();
                String finalSourceUrl = sourceUrl2 + "?key=" + keys[keyIndex];
                String finalBashPath = bashPath2;


                String finalFileName = fileName2;

                fixedThreadPool.execute(() -> {
                    OwsUtils.saveTile(finalSourceUrl, finalBashPath, finalFileName);
                    if (countTmp % 1000 == 0) {
                        log.info("z:{},Download Count: {}", Long.valueOf(tileGrid.z), Integer.valueOf(countTmp));
                    }

                }, 60000L);
            }
            log.info("");
        }
    }

    private static int getKeyIndex() {
        // 定义权重
        int[] weights = {9, 9, 10};

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

    public static void main(String[] args) {
        int n = 100;
        for (int i = 0; i < n; i++) {
            System.out.println(getKeyIndex());
        }
    }
}