package com.zhuanzhu;

import com.zhuanzhu.domain.DownloadConfig;
import com.zhuanzhu.domain.PercentageVO;
import com.zhuanzhu.utils.ListUtil;
import com.zhuanzhu.utils.OwsUtils;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Liwq
 */
public class TileDownloader {
    private static final Logger log = LoggerFactory.getLogger(TileDownloader.class);

    ThreadPoolExecutor poolExecutor = null;

    private DownloadConfig downloadConfig;

    public TileDownloader() {

    }

    public TileDownloader(DownloadConfig downloadConfig) {
        this.downloadConfig = downloadConfig;
    }

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

                String[] keys = {"4FA3YUrnk4ufid6LY7kX", "nnPir3cnsIk5yzorthF7", "FrEn078v226J4kM6MT7o", "2YPjStF2tqFZhzPDXEDM", "Eq6PIKigXVHgeJ7hN9BD"};
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
        int[] weights = {9, 9, 10, 10, 10};

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

    public void exe() throws Exception {
        CoordinateReferenceSystem crs = CRS.decode("EPSG:3857");
        ReferencedEnvelope envelope = downloadConfig.getWgsEnvelope().transform(crs, false);
        List<TileGrid> allTileGrids = new LinkedList<>();
        for (int z = downloadConfig.getLevel().getMin(); z <= downloadConfig.getLevel().getMax(); z++) {
            log.info("{}", z);
            LinkedList<TileGrid> tileGrids = TileGrid.getTilesFromZ(envelope, z);
            log.info("z:{},{}", z, tileGrids.size());
            allTileGrids.addAll(tileGrids);
            File file = new File(downloadConfig.getSavePath() + z);
            if (!file.exists() && file.mkdirs()) {
                log.info("层级目录创建成功");
            }
        }
        log.info("allTileGrids:{}", allTileGrids.size());

        int pageSize = 100;
        final List<List<TileGrid>> list = ListUtil.getList(allTileGrids, pageSize);
        int pageCount = list.size();

        PercentageVO percentageVO = new PercentageVO((long) pageCount, "Download percentage:");
        ThreadPoolTaskExecutor fixedThreadPool = new ThreadPoolTaskExecutor();
        fixedThreadPool.setCorePoolSize(8);
        fixedThreadPool.setMaxPoolSize(16);
        fixedThreadPool.setQueueCapacity(pageSize);
        fixedThreadPool.setThreadNamePrefix("tile download -");
        fixedThreadPool.initialize();

        String savePath = downloadConfig.getSavePath();
        savePath = savePath + "{z}" + "/{x}-{y}." + downloadConfig.getFileSuffix();
        for (int i = 0; i < pageCount; i++) {
            percentageVO.showRate(i);
            final List<TileGrid> tileGrids = list.get(i);
            final CountDownLatch countDownLatch = new CountDownLatch(tileGrids.size());
            for (TileGrid tileGrid : tileGrids) {
                long x = tileGrid.x;
                long y = Math.abs(tileGrid.y + 1L);
                long zz = tileGrid.z;
                final String downloadUrl = downloadConfig.getUrl().replace("{z}", String.valueOf(zz))
                        .replace("{x}", String.valueOf(x))
                        .replace("{y}", String.valueOf(y)) +
                        "?" + downloadConfig.getAuthKeyName() + "=" + downloadConfig.getKeyByWeight();
                final String finalBashPath = savePath.replace("{z}", String.valueOf(zz))
                        .replace("{x}", String.valueOf(x))
                        .replace("{y}", String.valueOf(y));
                fixedThreadPool.execute(() -> {
                    File file = new File(finalBashPath);
                    if (file.exists()) {
                        countDownLatch.countDown();
                        return;
                    }
                    URLConnection urlConnection = null;
                    try {
                        urlConnection = new URL(downloadUrl).openConnection();
                        urlConnection.setConnectTimeout(120000);
                        urlConnection.setConnectTimeout(120000);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (urlConnection == null) {
                        log.error("瓦片下载失败：【z】{}【x】{}【y】{}", zz, x, y);
                        return;
                    }
                    try (InputStream in = urlConnection.getInputStream()) {
                        Files.copy(in, Paths.get(finalBashPath));
                    } catch (Exception e) {
                        log.error("瓦片下载失败：【z】{}【x】{}【y】{}", zz, x, y);
                        e.printStackTrace();
                    }
                    countDownLatch.countDown();
                }, 60000L);
            }

            countDownLatch.await();
        }
        fixedThreadPool.shutdown();


    }
}