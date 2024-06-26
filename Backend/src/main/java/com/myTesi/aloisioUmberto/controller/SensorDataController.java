package com.myTesi.aloisioUmberto.controller;
import com.myTesi.aloisioUmberto.data.entities.SensorData;
import com.myTesi.aloisioUmberto.data.services.interfaces.SensorDataService;
import com.myTesi.aloisioUmberto.dto.New.NewSensorDataDto;
import com.myTesi.aloisioUmberto.dto.SensorDataDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "SensorData") //Name displayed on swagger
public class SensorDataController {

    private final SensorDataService sensorDataService;

    @Operation(summary = "Save sensor data", description = "Save new sensor data with optional file")
    @PostMapping(value = "/SensorData", consumes = {"multipart/form-data"})
    public ResponseEntity<SensorData> saveSensorData(@RequestPart("data") NewSensorDataDto newSensorDataDTO,@RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
        //TODO ADD TOKEN CHECK
        newSensorDataDTO.setDate(LocalDateTime.now());// Imposta la data e l'ora corrente
        if (file != null && !file.isEmpty()) {
            return ResponseEntity.ok(sensorDataService.save(file, newSensorDataDTO));
        }
        return null;
    }

    @GetMapping("/SensorData/get-all")
    public ResponseEntity<List<SensorDataDto>> getAllSensorData() {
        return ResponseEntity.ok(sensorDataService.getAllSensorData());
    }

    @GetMapping("/SensorData/latest")
    public ResponseEntity<List<SensorDataDto>> getAllSensorBy10Min() {
        return ResponseEntity.ok(sensorDataService.getAllSensorDataBy10Min());
    }

    @GetMapping("/SensorData/latest-by-type/{type}")
    public ResponseEntity<List<SensorDataDto>> getAllSensorBy10MinByType(@PathVariable String type) {
        return ResponseEntity.ok(sensorDataService.getAllSensorDataBy10MinByType(type));
    }

    @GetMapping("/SensorData/{id}")
    public ResponseEntity<SensorDataDto> getSensorDataById(@PathVariable String id) {
        return ResponseEntity.ok(sensorDataService.getSensorDataById(id));
    }

    @PutMapping("/SensorData/{id}")
    public ResponseEntity<SensorData> updateSensorData(@RequestBody SensorData newSensorData) {
        //TODO ADD TOKEN CHECK
        return ResponseEntity.ok(sensorDataService.update(newSensorData));
    }

    @DeleteMapping("/SensorData/{id}")
    public HttpStatus deleteSensorData(@PathVariable String id) {
        //TODO ADD TOKEN CHECK
        sensorDataService.delete(id);
        return HttpStatus.OK;
    }

    @GetMapping("/SensorData/processed/{type}")
    public ResponseEntity<String> getProcessedSensorData(@PathVariable String type) {
        String geoJson = sensorDataService.getProcessedSensorData(type);
        return ResponseEntity.ok(geoJson);
    }
}
