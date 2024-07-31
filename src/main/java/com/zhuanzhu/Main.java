package com.zhuanzhu;

import com.zhuanzhu.domain.DownloadConfig;

public class Main {
    public static void main(String[] args) throws Exception {
        final String notMsg = "# 下载的经纬度范围 经度 -180,180 纬度 -85,85\n" +
                "range:\n" +
                "  minlon: -180\n" +
                "  maxlon: 180\n" +
                "  minlat: -85\n" +
                "  maxlat: 85\n" +
                "# 下载的层级 0-18 层\n" +
                "level:\n" +
                "  max: 18\n" +
                "  min: 0\n" +
                "# 瓦片保存的路径\n" +
                "savePath: \"/data3/offlinemap/maptiler/sc/\"\n" +
                "# 瓦片下载的地址，{z}/{x}/{y}\n" +
                "url: \"https://api.maptiler.com/tiles/terrain-rgb-v2/{z}/{x}/{y}.webp\"\n" +
                "# 下载的文件后缀\n" +
                "fileSuffix: webp\n" +
                "# 下载是否需要认证key，不需要，则不用配置，且不会使 authKey 生效\n" +
                "authKeyName: key\n" +
                "# 下载认证的key，可以支持多个key（适用于单key有次数限制），且可以配置key调用的权重\n" +
                "authKeys:\n" +
                "    # key 值\n" +
                "  - key: 4FA3YUrnk4ufid6LY7kX\n" +
                "    # key 权重\n" +
                "    weight: 9\n" +
                "  - key: nnPir3cnsIk5yzorthF7\n" +
                "    weight: 9\n" +
                "  - key: FrEn078v226J4kM6MT7o\n" +
                "    weight: 9\n" +
                "  - key: 2YPjStF2tqFZhzPDXEDM\n" +
                "    weight: 9\n" +
                "  - key: Eq6PIKigXVHgeJ7hN9BD\n" +
                "    weight: 9";
        if (args == null || args.length == 0) {
            System.out.println("本JAR包用于离线地图瓦片下载");
            System.out.println("示例配置文件如下：");
            System.out.println(notMsg);
            return;
        }
        String configFileAbsPath = args[0];
        DownloadConfig downloadConfig = DownloadConfig.loadConfig(configFileAbsPath);

        if (downloadConfig == null) {
            System.out.println("配置文件加载失败");
            System.out.println("示例配置文件如下：");
            System.out.println(notMsg);
            return;
        }
        TileDownloader down = new TileDownloader(downloadConfig);
        down.exe();


//        int start = Integer.parseInt(args[0]);
//        int end = Integer.parseInt(args[1]);
//        double minLon = Double.parseDouble(args[2]);
//        double maxLon = Double.parseDouble(args[3]);
//        double minLat = Double.parseDouble(args[4]);
//        double maxLat = Double.parseDouble(args[5]);
//        String basePath = args[6];
//        ReferencedEnvelope wgsEnvelope = new ReferencedEnvelope(minLon, maxLon, minLat, maxLat, (CoordinateReferenceSystem) DefaultGeographicCRS.WGS84);
//        TileDownloader down = new TileDownloader();
////        https://api.maptiler.com/tiles/terrain-rgb-v2/{z}/{x}/{y}.webp?key=Eq6PIKigXVHgeJ7hN9BD
//        String sourceUrl = "https://api.maptiler.com/tiles/terrain-rgb-v2/{z}/{x}/{y}.webp?key=Eq6PIKigXVHgeJ7hN9BD";
//        if (args.length > 7) {
//            sourceUrl = args[7];
//        }
//        String downloadPath = "/{z}";
//        String fileName = "{x}-{y}.webp";
//        ThreadPoolTaskExecutor fixedThreadPool = new ThreadPoolTaskExecutor();
//        fixedThreadPool.initialize();
//        fixedThreadPool.setCorePoolSize(5);
//        fixedThreadPool.setMaxPoolSize(10);
//        if (!downloadPath.startsWith("/"))
//            downloadPath = "/" + downloadPath;
//        downloadPath = basePath + "/gmap" + downloadPath;
//        String finalDownloadPath = downloadPath;
//        down.amapDownload(sourceUrl, start, end, finalDownloadPath, fileName, wgsEnvelope, fixedThreadPool);
    }
}
