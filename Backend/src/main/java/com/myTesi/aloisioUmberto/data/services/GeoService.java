package com.myTesi.aloisioUmberto.data.services;


import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.springframework.stereotype.Service;

@Service
public class GeoService {


    private final GeometryFactory geometryFactory = new GeometryFactory();

    public boolean isSensorInInterestArea(double sensorLat, double sensorLon, String interestAreaWKT) {
        try {
            WKTReader reader = new WKTReader(geometryFactory);
            Geometry interestArea = reader.read(interestAreaWKT);

            Point sensorLocation = geometryFactory.createPoint(new Coordinate(sensorLon, sensorLat));
            return interestArea.contains(sensorLocation);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }
}