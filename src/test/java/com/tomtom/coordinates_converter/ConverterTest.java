package com.tomtom.coordinates_converter;


import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConverterTest {

    private Converter converter = new Converter();

    @Test
    public void checkValues() {
        converter.convertFromCoreDBCoordinates("[-750012700,622664710] [-749864320,622653590]", null);
        assertEquals("(-75.0012700 62.2664710, -74.9864320 62.2653590)", converter.getWgsCoordinates());
        assertEquals("LINESTRING (-75.0012700 62.2664710, -74.9864320 62.2653590)", converter.getWktCoordinates());
        assertEquals("[-750012700,622664710] [-749864320,622653590]", converter.getCoreDBCoordinates());
        assertEquals("[-750012700,622664710] [-749864320,622653590]", converter.getOriginalCoordinates());
    }

    @Test
    public void checkValuesWithChangeOrder() {
        converter.convertFromCoreDBCoordinates("[-750012700,622664710] [-749864320,622653590]", "change");
        assertEquals("(62.2664710 -75.0012700, 62.2653590 -74.9864320)", converter.getWgsCoordinates());
        assertEquals("LINESTRING (62.2664710 -75.0012700, 62.2653590 -74.9864320)", converter.getWktCoordinates());
        assertEquals("[622664710,-750012700] [622653590,-749864320]", converter.getCoreDBCoordinates());
        assertEquals("[-750012700,622664710] [-749864320,622653590]", converter.getOriginalCoordinates());
    }


}