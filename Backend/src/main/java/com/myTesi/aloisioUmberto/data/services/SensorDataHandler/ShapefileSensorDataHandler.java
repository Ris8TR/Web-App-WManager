package com.myTesi.aloisioUmberto.data.services.SensorDataHandler;

import com.myTesi.aloisioUmberto.data.services.SensorDataHandler.interfaces.SensorDataHandler;
import com.myTesi.aloisioUmberto.data.entities.SensorData;
import com.myTesi.aloisioUmberto.dto.New.NewSensorDataDto;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.*;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Component
public class ShapefileSensorDataHandler implements SensorDataHandler {

    @Override
    public void handle(SensorData data, NewSensorDataDto newSensorDataDTO, MultipartFile file) throws IOException, FactoryException, TransformException {
        data.setPayloadType("shapefile");

        // 1. Salva il file temporaneamente per permettere a GeoTools di leggerlo
        Path tempFile = Files.createTempFile("sensor_upload_", ".shp");
        file.transferTo(tempFile.toFile());

        try {
            FileDataStore store = FileDataStoreFinder.getDataStore(tempFile.toFile());
            SimpleFeatureCollection features = store.getFeatureSource().getFeatures();

            // 2. Gestione CRS (fondamentale per trasformare in Lat/Long)
            CoordinateReferenceSystem sourceCRS = store.getSchema().getCoordinateReferenceSystem();
            CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:4326"); // WGS84
            MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, true);

            try (SimpleFeatureIterator iterator = features.features()) {
                if (iterator.hasNext()) {
                    SimpleFeature feature = iterator.next();
                    Geometry jtsGeom = (Geometry) feature.getDefaultGeometry();
                    Geometry transformedGeom = org.geotools.geometry.jts.JTS.transform(jtsGeom, transform);   // WGS84
                    Map<String, Object> geoJsonMap = convertToGeoJsonMap(transformedGeom); //JTS -> Map compatibile con GeoJSON
                    data.setPayload(geoJsonMap);

                    if (transformedGeom.getCentroid() != null) {
                        data.setLatitude(transformedGeom.getCentroid().getY());
                        data.setLongitude(transformedGeom.getCentroid().getX());
                    }
                }
            }
            store.dispose();
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }


    private Map<String, Object> convertToGeoJsonMap(Geometry geometry) {
        Map<String, Object> geoJson = new HashMap<>();
        geoJson.put("type", geometry.getGeometryType());

        // Gestione delle coordinate in base al tipo di geometria
        if (geometry instanceof Point) {
            geoJson.put("coordinates", Arrays.asList(geometry.getCoordinate().getX(), geometry.getCoordinate().getY()));
        }
        else if (geometry instanceof LineString) {
            geoJson.put("coordinates", extractCoordinates(geometry));
        }
        else if (geometry instanceof Polygon) {
            geoJson.put("coordinates", extractPolygonCoordinates((Polygon) geometry));
        }
        else if (geometry instanceof MultiPolygon) {
            geoJson.put("coordinates", extractMultiPolygonCoordinates((MultiPolygon) geometry));
        }

        return geoJson;
    }

    private List<List<Double>> extractPolygonCoordinates(Polygon polygon) {
        List<List<Double>> coords = new ArrayList<>();
        coords.add(extractCoordinateList(polygon.getExteriorRing()));
        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
            coords.add(extractCoordinateList(polygon.getInteriorRingN(i)));
        }
        return coords;
    }

    private List<List<Double>> extractMultiPolygonCoordinates(MultiPolygon multiPolygon) {
        List<List<List<Double>>> allPolygons = new ArrayList<>();
        for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
            allPolygons.add(extractPolygonCoordinates((Polygon) multiPolygon.getGeometryN(i)));
        }
        return (List) allPolygons;
    }

    private List extractCoordinateList(LineString ring) {
        List<Double> list = new ArrayList<>();
        for (Coordinate c : ring.getCoordinates()) {
            list.add(c.getX());
            list.add(c.getY());
        }
        // GeoJSON vuole una lista di coppie [x,y],
        List<List<Double>> correctList = new ArrayList<>();
        for (int i = 0; i < list.size(); i += 2) {
            correctList.add(Arrays.asList(list.get(i), list.get(i+1)));
        }
        return correctList;
    }

    private List<List<Double>> extractCoordinates(Geometry geom) {
        List<List<Double>> coords = new ArrayList<>();
        for (Coordinate c : geom.getCoordinates()) {
            coords.add(Arrays.asList(c.getX(), c.getY()));
        }
        return coords;
    }
}
