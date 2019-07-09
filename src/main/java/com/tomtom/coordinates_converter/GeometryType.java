package com.tomtom.coordinates_converter;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum GeometryType {

     MULTILINESTRING ("MULTILINESTRING "),
     LINESTRING ("LINESTRING "),
     MULTIPOLYGON ("MULTIPOLYGON "),
     POLYGON ("POLYGON "),
     POINT ("POINT ");

     String enumValue;
}
