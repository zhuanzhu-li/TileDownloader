package test.test;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import test.test.TileDownloader;
public class Main {
    public static void main(String[] args) throws Exception {
        int start = Integer.parseInt(args[0]);
        int end = Integer.parseInt(args[1]);
        double minLon = Double.parseDouble(args[2]);
        double maxLon = Double.parseDouble(args[3]);
        double minLat = Double.parseDouble(args[4]);
        double maxLat = Double.parseDouble(args[5]);
        String basePath = args[6];
        ReferencedEnvelope wgsEnvelope = new ReferencedEnvelope(minLon, maxLon, minLat, maxLat, (CoordinateReferenceSystem)DefaultGeographicCRS.WGS84);
        TileDownloader down = new TileDownloader();
//        https://api.maptiler.com/tiles/terrain-rgb-v2/{z}/{x}/{y}.webp?key=Eq6PIKigXVHgeJ7hN9BD
        String sourceUrl = "https://api.maptiler.com/tiles/terrain-rgb-v2/{z}/{x}/{y}.webp?key=Eq6PIKigXVHgeJ7hN9BD";
        if(args.length>7){
            sourceUrl = args[7];
        }
        String downloadPath = "/{z}";
        String fileName = "{x}-{y}.webp";
        ThreadPoolTaskExecutor fixedThreadPool = new ThreadPoolTaskExecutor();
        fixedThreadPool.initialize();
        fixedThreadPool.setCorePoolSize(5);
        fixedThreadPool.setMaxPoolSize(10);
        if (!downloadPath.startsWith("/"))
            downloadPath = "/" + downloadPath;
        downloadPath = basePath + "/gmap" + downloadPath;
        String finalDownloadPath = downloadPath;
        down.amapDownload(sourceUrl, start, end, finalDownloadPath, fileName, wgsEnvelope, fixedThreadPool);
    }
}
