package com.zhuanzhu;


import lombok.Getter;
import org.opengis.referencing.FactoryException;

/**
 * tile num to tile bbox
 *
 * @author Liwq
 */
@Getter
public class TransBoundingBox {

    double north;
    double south;
    double east;
    double west;
    long x, y;
    int z;

    public TransBoundingBox() {

    }

    public TransBoundingBox(final long x, final long y, final int zoom) {
        this.north = tile2lat(y, zoom);
        this.south = tile2lat(y + 1, zoom);
        this.west = tile2lon(x, zoom);
        this.east = tile2lon(x + 1, zoom);
        this.x = x;
        this.y = y;
        this.z = zoom;
    }

    public static TransBoundingBox tile2boundingBox(final long x, final long y, final long zoom) {
        TransBoundingBox bb = new TransBoundingBox();
        bb.north = tile2lat(y, zoom);
        bb.south = tile2lat(y + 1, zoom);
        bb.west = tile2lon(x, zoom);
        bb.east = tile2lon(x + 1, zoom);
        return bb;
    }

    static double tile2lon(long x, long z) {
        return x / Math.pow(2.0, z) * 360.0 - 180;
    }

    static double tile2lat(long y, long z) {
        double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z);
        return Math.toDegrees(Math.atan(Math.sinh(n)));
    }

    @Override
    public String toString() {
        return "north:" + this.north + ",south:" + south + ",west:" + this.west + ",east:" + this.east;
    }
}
