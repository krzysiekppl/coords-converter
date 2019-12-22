package com.tomtom.coordinates_converter;

import com.tomtom.coordinates_converter.utils.GeoUtils;
import lombok.*;

import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.tomtom.coordinates_converter.GeometryType.*;

@Getter
@Setter
@NoArgsConstructor
@Service
class Converter {

    private final static String NOTHING = "";
    private final static String SPACE = " ";
    private final static String COMMA = ",";

    private final static String CORE_DB_COORDINATES_PATTERN = "\\[-?[\\d]{8,10},-?[\\d]{8,10}\\]";
    private final static String WGS_COORDINATES_PATTERN = "-?[1]?[\\d]{1,2}\\.?[\\d]* -?[1]?[\\d]{1,2}\\.?[\\d]*";
    private final static String WGS_COORDINATES_PATTERN_WITH_COMMA = "-?[1]?[\\d]{1,2}\\.?[\\d]*, -?[1]?[\\d]{1,2}\\.?[\\d]*";
    private final static String WKT_GEOMETRY_PATTERN = "^[a-zA-Z]+ (\\({1,3}(-?[1]?[\\d]{1,2}\\.?[\\d]+? -?[1]?[\\d]{1,2}\\.?[\\d]+?,? ?)+[\\)]{1,3},?)+";
    private final static String JSON_GEOMETRY_PATTERN = "[\\[\n]+-?[1]?[\\d]+,\n-?";

    private static final double COREDB_SCALE_FACTOR = 10_000_000;

    private String originalCoordinates;
    private String[] cartopiaCoordinates;
    private String coreDBCoordinates;
    private String utmCoordinates;
    private String wgsCoordinates;
    private String wktCoordinates;
    private String xmlCoordinates;
    private Geometry geometry;
    private int length;
    private String geoJSON;
    private Integer area;
    private List<Double[]> lineOnMap;

    // TODO verify login - find another parser
    void convertCoordinates(String coords, String order, Boolean reverse) {
        originalCoordinates = coords;
        System.out.println(coords);
        if(checkInputCoordinatesByRegex(coords,WKT_GEOMETRY_PATTERN)){
            convertFromWellKnownText(coords);
        }
       else if (checkInputCoordinatesByRegex(coords, CORE_DB_COORDINATES_PATTERN) || checkInputCoordinatesByRegex(coords,JSON_GEOMETRY_PATTERN)) {
            convertFromWellKnownText(convertFromCoreDBCoordinates(prepareString(coords).replace("]]],[[[", ":")
                    .replace("]],[[", ";")
                    .replace(COMMA, SPACE)
                    .replace("] [", COMMA)
                    .replace("[", NOTHING)
                    .replace("]", NOTHING), order, COREDB_SCALE_FACTOR));
        } else if (checkInputCoordinatesByRegex(coords, WGS_COORDINATES_PATTERN) || checkInputCoordinatesByRegex(coords, WKT_GEOMETRY_PATTERN)) {
            convertFromWellKnownText(convertFromCoreDBCoordinates(prepareString(coords)
                    .replace(")),((", ":")
                    .replace("),(", ";")
                    .replace(", ", COMMA), order, 1d));
        } else if (checkInputCoordinatesByRegex(coords, WGS_COORDINATES_PATTERN_WITH_COMMA)) {
            convertFromWellKnownText(convertFromCoreDBCoordinates(prepareString(coords), order, 1d));
        }//TODO Add new format
        prepareOutput(reverse);
       geoJSON = new GeometryJSON().toString(geometry);
//       areaInSquareM(geometry);
    }

    private String prepareString(String coords) {
        coords = coords.replace("\"", NOTHING)
                .replace("<", NOTHING)
                .replace(">", NOTHING)
                .replace(";", NOTHING)
                .replace(":", NOTHING)
                .replace("\n", NOTHING)
                .replace("\r", NOTHING)
                .replaceAll("[a-zA-Z]+", NOTHING);
        return coords.trim();
    }

    private void prepareOutput(Boolean reverse) {
        if (reverse) {
            geometry = geometry.reverse();
            wktCoordinates = geometry.toString();
        }
        wgsCoordinates = wktCoordinates.replace(MULTILINESTRING.enumValue, NOTHING)
                .replace(MULTIPOLYGON.enumValue, NOTHING).replace(POLYGON.enumValue, NOTHING).replace(LINESTRING.enumValue, NOTHING).replace(GeometryType.POINT.enumValue, NOTHING);
        cartopiaCoordinates = wgsCoordinates.replace(SPACE, COMMA).split(COMMA + COMMA);
        coreDBCoordinates = wgsCoordinates.replace(".", NOTHING)
                .replace(SPACE, COMMA)
                .replace(COMMA + COMMA, "],[")
                .replace("(", "[")
                .replace(")", "]");
        xmlCoordinates = coreDBCoordinates.replace("],[", "] [");
        prepareLineOnMap();
        length = (int) new GeoUtils().geometryLength(geometry);
    }


    private void prepareLineOnMap() {
        lineOnMap = new ArrayList<>();
        for (Coordinate coordinate : geometry.getCoordinates()) {
            lineOnMap.add(new Double[]{coordinate.y, coordinate.x});
        }
    }

    private void convertFromWellKnownText(String coords) {
        try {
            geometry = new WKTReader().read(coords);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        wktCoordinates = coords;
    }

    private boolean checkInputCoordinatesByRegex(String stringToMatch, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(stringToMatch);
        return matcher.find();
    }

    // TODO use another parser
    private String convertFromCoreDBCoordinates(String coords, String order, Double scale) {
        String modifyCoordinates = coords.trim();
        String[] strings = modifyCoordinates.split(":");
        List<Coordinate> coordinatesTemp = new ArrayList<>();
        setWktCoordinates("(");
        String tempOuteringInnering = "";
        for (String string : strings) {
            String[] temp = string.split(";");
            tempOuteringInnering += "(";
            for (String s : temp) {
                String tempwkt = "(";
                String[] outeringInnering = s.replace("(", "").replace(")", "").split(",");
                Coordinate coordss;
                for (String stringCoordinates : outeringInnering) {
                    tempwkt += getCoordinatesStringFormat(stringCoordinates.split(" "), order, scale) + ", ";
                    coordss = new GeoUtils().getCoordinatesCoordsFormat(stringCoordinates.split(" "));
                    coordinatesTemp.add(coordss);

                    if (coordinatesTemp.size() >= 4 && coordinatesTemp.size() == outeringInnering.length && coordinatesTemp.get(0).equals2D(coordinatesTemp.get(coordinatesTemp.size() - 1)) && strings.length == 1) {
                        setWktCoordinates(getWktCoordinates().replaceFirst("\\(", POLYGON.enumValue));
                    } else if (coordinatesTemp.size() > 3 && coordinatesTemp.size() == outeringInnering.length && coordinatesTemp.get(0).equals2D(coordinatesTemp.get(coordinatesTemp.size() - 1))
                            && strings.length > 1 && wktCoordinates.contains("POLYGON")) {
                        setWktCoordinates(getWktCoordinates().replaceFirst("\\(", MULTIPOLYGON.enumValue + "("));
                    } else if (temp.length > 1 && coordinatesTemp.size() == outeringInnering.length && !coordinatesTemp.get(0).equals2D(coordinatesTemp.get(coordinatesTemp.size() - 1))) {
                        setWktCoordinates(getWktCoordinates().replaceFirst("\\(", MULTILINESTRING.enumValue));
                    } else if (temp.length == 1 && coordinatesTemp.size() == outeringInnering.length && !coordinatesTemp.get(0).equals2D(coordinatesTemp.get(coordinatesTemp.size() - 1))) {
                        String wktCoordinates = LINESTRING.enumValue + tempwkt.substring(0, tempwkt.length() - 2) + ")";
                        setWktCoordinates(wktCoordinates);
                        return wktCoordinates;
                    } else if (outeringInnering.length == 1) {
                        setWktCoordinates(POINT.enumValue + tempwkt.replace(", ", ")"));
                        return POINT + tempwkt.replace(", ", ")");
                    }
                }
                tempOuteringInnering += tempwkt.substring(0, tempwkt.length() - 2) + ")";
            }
            tempOuteringInnering += ")";
        }
        wktCoordinates += tempOuteringInnering;
        if (wktCoordinates.contains(MULTIPOLYGON.enumValue)) wktCoordinates = wktCoordinates + ")";
        return wktCoordinates.replace(")(", "),(");
    }

    private static String getCoordinatesStringFormat(String[] partsOfCoordinate, String order, Double coreDBScale) {
        String coords;
        if (!"change".equals(order)) {
            coords = String.format("%.7f", Double.parseDouble(partsOfCoordinate[0]) / coreDBScale) + " " +
                    String.format("%.7f", Double.parseDouble(partsOfCoordinate[1]) / coreDBScale);
        } else {
            coords = String.format("%.7f", Double.parseDouble(partsOfCoordinate[1]) / coreDBScale) + " " +
                    String.format("%.7f", Double.parseDouble(partsOfCoordinate[0]) / coreDBScale);
        }
        return coords;
    }

    private void areaInSquareM(Geometry geometry){
        try {
            MathTransform transform = CRS.findMathTransform(CRS.decode("EPSG:4326"), CRS.decode("EPSG:3857"), true);
            area = (int) JTS.transform(geometry, transform).getArea();
        } catch (FactoryException | TransformException e) {
            e.printStackTrace();
        }
        System.out.println(area);
    }
}
