package com.zhuanzhu;

import com.zhuanzhu.gis.CustomSTRtree;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geojson.feature.FeatureJSON;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.index.strtree.STRtree;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.InputStream;

/**
 * @author Liwq
 */
public class BoundRtreeCache {

    public static CustomSTRtree stRtree;

    static class RtreeItem {
        private final Geometry geometry;

        RtreeItem(Geometry geometry) {
            this.geometry = geometry;

        }
    }

    @SuppressWarnings("unchecked")
    public static void init() {
        ClassLoader classLoader = BoundRtreeCache.class.getClassLoader();
        FeatureJSON featureJSON = new FeatureJSON();
        try (InputStream inputStream = classLoader.getResourceAsStream("gis/china_all_grid1r.geojson");) {
            FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = (FeatureCollection<SimpleFeatureType, SimpleFeature>) featureJSON.readFeatureCollection(inputStream);
            FeatureIterator<SimpleFeature> features = featureCollection.features();
            stRtree = new CustomSTRtree(featureCollection.size());
            while (features.hasNext()) {
                SimpleFeature feature = features.next();
                RtreeItem rtreeItem = new RtreeItem((Geometry) feature.getDefaultGeometry());
                stRtree.insert(rtreeItem.geometry.getEnvelopeInternal(), rtreeItem);
            }
            stRtree.build();
            features.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
