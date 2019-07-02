package com.tomtom.coordinates_converter;

import lombok.*;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
@NoArgsConstructor
@Service
public class Converter {

    private String originalCoordinates;
    private String[] cartopiaCoordinates;
    private String coreDBCoordinates;
    private String utmCoordinates;
    private String wgsCoordinates;
    private String wktCoordinates;

    void checkInputCoordinates(String coords, String order) {

        if (checkInputCoordinatesByRegex(coords, "\\[-?[\\d]{8,10},-?[\\d]{8,10}\\]")) {
            convertFromCoreDBCoordinates(coords, order);
        } else if (checkInputCoordinatesByRegex(coords, "-?[1]?[\\d]{1,2}\\.[\\d]* -?[1]?[\\d]{1,2}\\.[\\d]*")) {
            convertFromWGSCoordinates(coords);
        } else if (checkInputCoordinatesByRegex(coords, "-?[1]?[\\d]{1,2}\\.[\\d]*, -?[1]?[\\d]{1,2}\\.[\\d]*")) {

        }
    }

    boolean checkInputCoordinatesByRegex(String stringToMatch, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(stringToMatch);
        return matcher.find();
    }

    public void convertFromWGSCoordinates(String coords) {
        originalCoordinates = coords.trim();
        wgsCoordinates = originalCoordinates;
        prepareWKTCoords();
        prepareCoreDBCoords();
        prepareCartopiaCoords();
    }

    public void convertFromCoreDBCoordinates(String coords, String order) {
        originalCoordinates = coords.trim();
        String[] listOfCoordinates = originalCoordinates.substring(1, originalCoordinates.length() - 1).split("\\]\\s*\\[");
        StringBuilder wgsCoordinatesBuilder = new StringBuilder();
        wgsCoordinatesBuilder.append("(");
        for (String coordinates : listOfCoordinates) {
            String[] partsOfCoordinate = coordinates.trim().split(",");
            String longitude = getCoordinatesFormat(0, partsOfCoordinate);
            String latitude = getCoordinatesFormat(1, partsOfCoordinate);
            if (!"change".equals(order)) {
                wgsCoordinatesBuilder.append(longitude).append(" ").append(latitude).append(", ");
            } else {
                wgsCoordinatesBuilder.append(latitude).append(" ").append(longitude).append(", ");
            }
        }
        wgsCoordinates = wgsCoordinatesBuilder.toString().substring(0, wgsCoordinatesBuilder.length() - 2) + ")";
        prepareWKTCoords();
        prepareCoreDBCoords();
        prepareCartopiaCoords();
    }

    private void prepareCartopiaCoords() {
        cartopiaCoordinates=wgsCoordinates.replace("(","")
                .replace(")","")
                .replace(" ",",")
                .split(",,");
    }

    private void prepareWKTCoords() {
        if (wgsCoordinates.length() < 25) {
            wktCoordinates = "POINT " + wgsCoordinates;
        } else {
            wktCoordinates = "LINESTRING " + wgsCoordinates;
        }
    }

    private void prepareCoreDBCoords() {
        coreDBCoordinates = wgsCoordinates.trim()
                .replace(", ", "];[")
                .replace(" ", ",")
                .replace("(", "[")
                .replace(")", "]")
                .replace(".", "")
                .replace(";", " ");
    }

    private String getCoordinatesFormat(int i, String[] partsOfCoordinate) {
        return String.format("%.7f", Double.parseDouble(partsOfCoordinate[i]) / 10000000);
    }
}
