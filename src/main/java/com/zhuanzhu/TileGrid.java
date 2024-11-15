package com.zhuanzhu;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import lombok.Getter;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

/**
 * @author Liwq
 */
public class TileGrid implements Serializable{

    protected long x;

    protected long y;

    @Getter
    protected long z;

    protected ReferencedEnvelope envelope;

    public static final CoordinateReferenceSystem WGS84;

    public static final double EXTENT_OFFSET = 2.00375083427892E7D;

    public static final double MAX_TILE_WIDTH;

    public static final ReferencedEnvelope WORLD_BOUNDS;

    public static final int TILE_WIDTH = 256;

    public static final int TILE_HEIGHT = 256;

    public static final CoordinateReferenceSystem TILE_CRS;

    public static final double METERS_PER_UNIT = 1.0D;

    GeometryFactory gg = new GeometryFactory();

    public static final List<Double> RESOLUTIONS = Arrays.asList(156543.03390625D,
            78271.516953125D,
            39135.7584765625D,
            19567.87923828125D,
            9783.939619140625D,
            4891.9698095703125D,
            2445.9849047851562D,
            1222.9924523925781D,
            611.4962261962891D,
            305.74811309814453D,
            152.87405654907226D,
            76.43702827453613D,
            38.218514137268066D,
            19.109257068634033D,
            9.554628534317017D,
            4.777314267158508D,
            2.388657133579254D,
            1.194328566789627D,
            0.5971642833948135D,
            0.2985821416974068D,
            0.1492910708487034D,
            0.0746455354243517D,
            0.0373227677121758D,
            0.0186613838560879D,
            0.009330691928044D,
            0.004665345964022D,
            0.002332672982011D,
            0.0011663364910055D,
            5.831682455027E-4D,
            2.915841227514E-4D,
            1.457920613757E-4D);

    static {
        try {
            WGS84 = DefaultGeographicCRS.WGS84;
            TILE_CRS = CRS.decode("EPSG:3857");
            WORLD_BOUNDS = new ReferencedEnvelope(new Envelope(-2.00375083427892E7D, 2.00375083427892E7D, 2.00375083427892E7D, -2.00375083427892E7D), TILE_CRS);
            MAX_TILE_WIDTH = 2.00375083427892E7D;
        } catch (Exception e) {
            throw new RuntimeException("Could not initialize the class constants", e);
        }
    }

    public TileGrid(long x, long y, long z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.envelope = envelope(x, y, z);
    }

    public TileGrid() {}

    public boolean contains(double x, double y) {
        double minx = this.envelope.getMinX();
        double maxx = this.envelope.getMaxX();
        double miny = this.envelope.getMinY();
        double maxy = this.envelope.getMaxY();
        return x >= minx && x < maxx && y >= miny && y < maxy;
    }

    public TileGrid(ReferencedEnvelope envelope) {
        double tileSize = envelope.getMaxX() - envelope.getMinX();
        this.x = Math.round((envelope
                .getMinimum(0) - WORLD_BOUNDS.getMinimum(0)) / tileSize);
        this.y = Math.round((envelope
                .getMinimum(1) + WORLD_BOUNDS.getMinimum(1)) / tileSize);
        this.envelope = envelope;
    }

    public TileGrid getParent() {
        if (this.z == 0L) {
            return null;
        }
        return new TileGrid((long)Math.floor(this.x / 2.0D), (long)Math.floor(this.y / 2.0D), this.z - 1L);
    }

    public TileGrid getParentToZ(long targetZ) {
        TileGrid target = this;
        if (this.z < targetZ) {
            throw new IllegalArgumentException("Illegal Argument.");
        }
        while (target.getZ() > targetZ) {
            target = target.getParent();
        }
        return target;
    }

    public TileGrid[] getChildren() {
        TileGrid[] result = new TileGrid[4];
        result[0] = new TileGrid(this.x * 2L, this.y * 2L, this.z + 1L);
        result[1] = new TileGrid(this.x * 2L + 1L, this.y * 2L, this.z + 1L);
        result[2] = new TileGrid(this.x * 2L, this.y * 2L + 1L, this.z + 1L);
        result[3] = new TileGrid(this.x * 2L + 1L, this.y * 2L + 1L, this.z + 1L);
        return result;
    }

    public static LinkedList<TileGrid> getChildrenToZ(long x, long y, long z, long targetZ) {
        LinkedList<TileGrid> targetTileGrids = new LinkedList<>();
        if (z == targetZ) {
            targetTileGrids.add(new TileGrid(x, y, z));
            return targetTileGrids;
        }
        targetTileGrids.addAll(getChildrenToZ(x * 2L, y * 2L, z + 1L, targetZ));
        targetTileGrids.addAll(getChildrenToZ(x * 2L + 1L, y * 2L, z + 1L, targetZ));
        targetTileGrids.addAll(getChildrenToZ(x * 2L, y * 2L + 1L, z + 1L, targetZ));
        targetTileGrids.addAll(getChildrenToZ(x * 2L + 1L, y * 2L + 1L, z + 1L, targetZ));
        return targetTileGrids;
    }

    public static LinkedList<TileGrid> getTileGridsToZ(ReferencedEnvelope envelope, long z) {
        LinkedList<TileGrid> targetTileGrids = new LinkedList<>();
        TileGrid tileGrid = new TileGrid(envelope);
        if (tileGrid.getZ() > z) {
            targetTileGrids.add(tileGrid.getParentToZ(z));
        } else if (tileGrid.getZ() == z) {
            targetTileGrids.add(tileGrid);
        } else if (tileGrid.getZ() < z) {
            targetTileGrids.addAll(getChildrenToZ(tileGrid.x, tileGrid.y, tileGrid.z, z));
        }
        return targetTileGrids;
    }

    protected ReferencedEnvelope envelope(long x, long y, long z) {
        double resolution = RESOLUTIONS.get((int) z);
        double tileSize = 256.0D * resolution;
        double xMin = x * tileSize + WORLD_BOUNDS.getMinX();
        double yMin = y * tileSize - WORLD_BOUNDS.getMinY();
        return new ReferencedEnvelope(xMin, xMin + tileSize, yMin, yMin + tileSize, TILE_CRS);
    }

    public Polygon referencedEnvelopeToPolygon() {
        TransBoundingBox transBoundingBox = TransBoundingBox.tile2boundingBox(x, Math.abs(y + 1L), z);
        return gg.createPolygon(gg.createLinearRing(new Coordinate[]{
                new Coordinate(transBoundingBox.west, transBoundingBox.north),
                new Coordinate(transBoundingBox.east, transBoundingBox.north),
                new Coordinate(transBoundingBox.east, transBoundingBox.south),
                new Coordinate(transBoundingBox.west, transBoundingBox.south),
                new Coordinate(transBoundingBox.west, transBoundingBox.north)
        }), null);
    }
    public ReferencedEnvelope referencedEnvelope() {
        TransBoundingBox transBoundingBox = TransBoundingBox.tile2boundingBox(x, Math.abs(y + 1L), z);
        return new ReferencedEnvelope(transBoundingBox.west, transBoundingBox.east, transBoundingBox.south, transBoundingBox.north, TILE_CRS);
    }

    @Override
    public String toString() {
        return "Tile X: " + this.x + ", Y: " + this.y + ", Z: " + this.z + " (" + this.envelope + ")";
    }

    public static ArrayList<TileGrid> getTilesFromZ(ReferencedEnvelope envelope, int z) {
        double resolution = RESOLUTIONS.get(z);
        double tileSize = 256.0D * resolution;
        long minX = (long)Math.floor((envelope
                .getMinimum(0) - WORLD_BOUNDS.getMinimum(0)) / tileSize);
        long minY = (long)Math.floor((envelope
                .getMinimum(1) + WORLD_BOUNDS.getMinimum(1)) / tileSize);
        long maxX = (long)Math.floor((envelope
                .getMaximum(0) - WORLD_BOUNDS.getMinimum(0)) / tileSize);
        long maxY = (long)Math.floor((envelope
                .getMaximum(1) + WORLD_BOUNDS.getMinimum(1)) / tileSize);
        ArrayList<TileGrid> tileGrids = new ArrayList<>();
        long i;
        for (i = minX; i <= maxX; i++) {
            long j;
            for (j = minY; j <= maxY; j++) {
                tileGrids.add(new TileGrid(i, j, z));
            }
        }
        return tileGrids;
    }
}
