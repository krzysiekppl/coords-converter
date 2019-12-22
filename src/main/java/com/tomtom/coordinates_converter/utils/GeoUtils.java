package com.tomtom.coordinates_converter.utils;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.opengis.referencing.operation.TransformException;

public class GeoUtils {

    private static final double COREDB_SCALE_FACTOR = 10_000_000;


    public Coordinate getCoordinatesCoordsFormat(String[] partsOfCoordinate) {
        return new Coordinate(Double.parseDouble(partsOfCoordinate[0]) / COREDB_SCALE_FACTOR,
                Double.parseDouble(partsOfCoordinate[1]) / COREDB_SCALE_FACTOR);
    }

    public double geometryLength(Geometry geometry) {
        double length = 0;
        for (int i = 0; i < geometry.getCoordinates().length - 1; i++) {
            length += distanceInMeters(geometry.getCoordinates()[i], geometry.getCoordinates()[i + 1]);
        }
        return length;
    }

    private double distanceInMeters(Coordinate c1, Coordinate c2) {
        try {
            return JTS.orthodromicDistance(c1, c2, DefaultGeographicCRS.WGS84);
        } catch (TransformException e) {
            throw new IllegalStateException(e);
        }
    }
}
