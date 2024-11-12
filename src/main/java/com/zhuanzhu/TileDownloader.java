package com.zhuanzhu;

import com.zhuanzhu.domain.DownloadConfig;
import com.zhuanzhu.domain.PercentageVO;
import com.zhuanzhu.utils.ListUtil;
import org.apache.commons.lang3.StringUtils;
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
import java.util.concurrent.CountDownLatch;

/**
 * @author Liwq
 */
public class TileDownloader {
    private static final Logger log = LoggerFactory.getLogger(TileDownloader.class);

    private final DownloadConfig downloadConfig;

    public TileDownloader(DownloadConfig downloadConfig) {
        this.downloadConfig = downloadConfig;
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
                        (StringUtils.isNotEmpty(downloadConfig.getAuthKeyName()) ? "?" + downloadConfig.getAuthKeyName() + "=" + downloadConfig.getKeyByWeight() : "")
                        ;
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