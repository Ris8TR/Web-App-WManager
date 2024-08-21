package com.AloisioUmerto.Tesi.DataHandler.controller;


import com.AloisioUmerto.Tesi.DataHandler.data.entities.SensorData;
import com.AloisioUmerto.Tesi.DataHandler.data.service.SensorDataService;
import com.AloisioUmerto.Tesi.DataHandler.dto.NewSensorDataDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "SensorData") //Name displayed on swagger
public class SensorDataController {
    private final SensorDataService sensorDataService;

    @Operation(summary = "Save sensor data", description = "Save new sensor data with optional file")
    @PostMapping(value = "/SensorData", consumes = {"multipart/form-data"})
    public ResponseEntity<SensorData> saveSensorData(
            @RequestPart("data") NewSensorDataDto newSensorDataDTO,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
        return ResponseEntity.ok(sensorDataService.save(file, newSensorDataDTO));

    }

}