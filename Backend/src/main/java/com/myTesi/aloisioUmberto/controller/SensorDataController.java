package com.myTesi.aloisioUmberto.controller;
import com.myTesi.aloisioUmberto.data.entities.SensorData;
import com.myTesi.aloisioUmberto.data.services.interfaces.SensorDataService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "SensorData") //Name displayed on swagger
public class SensorDataController {

    private final SensorDataService sensorDataService;

    @PostMapping("/SensorData")
    public ResponseEntity<Void> saveSensorData(@RequestBody SensorData sensorData) {
        sensorDataService.saveSensorData(sensorData);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/SensorData/get-all")
    public ResponseEntity<List<SensorData>> getAllSensorData() {
        List<SensorData> sensorDataList = sensorDataService.getAllSensorData();
        return ResponseEntity.ok(sensorDataList);
    }

    @GetMapping("/SensorData/{id}")
    public ResponseEntity<SensorData> getSensorDataById(@PathVariable String id) {
        SensorData sensorData = sensorDataService.getSensorDataById(id);
        if (sensorData == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(sensorData);
    }

    @PutMapping("/SensorData/{id}")
    public ResponseEntity<Void> updateSensorData(@PathVariable String id, @RequestBody SensorData newSensorData) {
        sensorDataService.updateSensorData(id, newSensorData);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/SensorData/{id}")
    public ResponseEntity<Void> deleteSensorData(@PathVariable String id) {
        sensorDataService.deleteSensorData(id);
        return ResponseEntity.noContent().build();
    }
}
