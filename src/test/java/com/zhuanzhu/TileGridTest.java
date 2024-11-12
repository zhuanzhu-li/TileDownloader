package com.zhuanzhu;

import junit.framework.TestCase;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

public class TileGridTest extends TestCase {

    public void testEnvelope() throws Exception {
        GeometryFactory gg = new GeometryFactory();
        TileGrid tileGrid = new TileGrid(3235, 1680, 12);
        ReferencedEnvelope envelope = tileGrid.envelope;
        System.out.println(envelope);
        Polygon polygon = gg.createPolygon(gg.createLinearRing(new Coordinate[]{
                new Coordinate(envelope.getMinX(), envelope.getMinY()),
                new Coordinate(envelope.getMaxX(), envelope.getMinY()),
                new Coordinate(envelope.getMaxX(), envelope.getMaxY()),
                new Coordinate(envelope.getMinX(), envelope.getMaxY()),
                new Coordinate(envelope.getMinX(), envelope.getMinY())
        }), null);
        System.out.println(polygon);
        DefaultGeographicCRS wgs84 = DefaultGeographicCRS.WGS84;
        ReferencedEnvelope transform = envelope.transform(wgs84, true);
        System.out.println(transform);
        System.out.println(tileGrid.referencedEnvelopeToPolygon());
    }
}