package com.zhuanzhu;

import junit.framework.TestCase;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

public class TileGridTest extends TestCase {

    public void testEnvelope() throws Exception {
        TileGrid tileGrid = new TileGrid(1613, 841, 11);
        ReferencedEnvelope envelope = tileGrid.envelope;
        System.out.println(envelope);
        DefaultGeographicCRS wgs84 = DefaultGeographicCRS.WGS84;
        ReferencedEnvelope transform = envelope.transform(wgs84, true);
        System.out.println(transform);
        System.out.println(tileGrid.referencedEnvelopeToPolygon());
    }
}