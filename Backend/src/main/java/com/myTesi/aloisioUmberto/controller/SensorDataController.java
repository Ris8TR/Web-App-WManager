package com.myTesi.aloisioUmberto.controller;
import com.myTesi.aloisioUmberto.data.entities.SensorData;
import com.myTesi.aloisioUmberto.data.services.interfaces.SensorDataService;
import com.myTesi.aloisioUmberto.dto.DateDto;
import com.myTesi.aloisioUmberto.dto.New.NewSensorDataDto;
import com.myTesi.aloisioUmberto.dto.SensorDataDto;
import com.myTesi.aloisioUmberto.dto.SensorDataInterestAreaDto;
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
@CrossOrigin(origins = "http://192.168.15.34:4200")
@Tag(name = "SensorData") //Name displayed on swagger
public class SensorDataController {

    private final SensorDataService sensorDataService;


    @Operation(summary = "Receive sensor data", description = "Receive and save sensor data from micro-service")
    @PostMapping("/sensorData")
    public ResponseEntity<SensorData> receiveSensorData(@RequestBody NewSensorDataDto newSensorDataDto) {
        return ResponseEntity.ok(sensorDataService.saveSensorData(newSensorDataDto));
    }

    @Operation(summary = "Save sensor data", description = "Save new sensor data with optional file")
    @PostMapping(value = "/SaveSensorData", consumes = {"multipart/form-data"})
    public ResponseEntity<SensorData> saveSensorData(
            @RequestPart("data") NewSensorDataDto newSensorDataDTO,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
        System.out.println(newSensorDataDTO);
        return ResponseEntity.ok(sensorDataService.save(file, newSensorDataDTO));
    }


    @GetMapping("/SensorData/top-by-interestAreaId/{interestAreaId}")
    public ResponseEntity<List<SensorDataDto>> getLastSensorDataByInterestAreaId(@PathVariable String interestAreaId) {
        return ResponseEntity.ok(sensorDataService.getTopSensorDataByInterestAreaId(interestAreaId));
    }
    @GetMapping("/SensorData/top-by-sensor-interestAreaId/{interestAreaId}/{sensorId}")
    public ResponseEntity<List<SensorDataDto>> getLastSensorDataBySensorAndInterestAreaId(@PathVariable String interestAreaId, @PathVariable String sensorId) {
        return ResponseEntity.ok(sensorDataService.getTopSensorDataByInterestAreaIdAndSensorId(interestAreaId, sensorId));
    }
    @GetMapping("/SensorData/top-by-sensorId/{sensorId}")
    public ResponseEntity<SensorDataDto> getLastSensorDataBySensor(@PathVariable String sensorId) {
        return ResponseEntity.ok(sensorDataService.getTopSensorDataBySensorId(sensorId));
    }


    @GetMapping("/SensorData/last5m-by-interestAreaId/{interestAreaId}")
    public ResponseEntity<List<SensorDataDto>> getAllSensorDataByInterestAreaId5Min(@PathVariable String interestAreaId) {
        return ResponseEntity.ok(sensorDataService.getAllSensorDataByInterestAreaId5Min(interestAreaId));
    }
    @GetMapping("/SensorData/last10m-by-interestAreaId/{interestAreaId}")
    public ResponseEntity<List<SensorDataDto>> getAllSensorDataByInterestAreaId10Min(@PathVariable String interestAreaId) {
        return ResponseEntity.ok(sensorDataService.getAllSensorDataByInterestAreaId10Min(interestAreaId));
    }
    @GetMapping("/SensorData/last15m-by-interestAreaId/{interestAreaId}")
    public ResponseEntity<List<SensorDataDto>> getAllSensorDataByInterestAreaId15Min(@PathVariable String interestAreaId) {
        return ResponseEntity.ok(sensorDataService.getAllSensorDataByInterestAreaId15Min(interestAreaId));
    }
    @GetMapping("/SensorData/last5m-by-sensor-interestAreaId/{interestAreaId}/{sensorId}")
    public ResponseEntity<List<SensorDataDto>> getAllSensorDataBySensorAndInterestAreaId5Min(@PathVariable String interestAreaId, @PathVariable String sensorId) {
        return ResponseEntity.ok(sensorDataService.getAllSensorDataByInterestAreaId5Min(interestAreaId));
    }
    @GetMapping("/SensorData/last10m-by-sensor-interestAreaId/{interestAreaId}/{sensorId}")
    public ResponseEntity<List<SensorDataDto>> getAllSensorDataBySensorAndInterestAreaId10Min(@PathVariable String interestAreaId, @PathVariable String sensorId) {
        return ResponseEntity.ok(sensorDataService.getAllSensorDataByInterestAreaId10Min(interestAreaId));
    }
    @GetMapping("/SensorData/last15m-by-sensor-interestAreaId/{interestAreaId}/{sensorId}")
    public ResponseEntity<List<SensorDataDto>> getAllSensorDataBySensorAndInterestAreaId15Min(@PathVariable String interestAreaId, @PathVariable String sensorId) {
        return ResponseEntity.ok(sensorDataService.getAllSensorDataByInterestAreaId15Min(interestAreaId));
    }
    @GetMapping("/SensorData/last5m-by-sensorId/{sensorId}")
    public ResponseEntity<List<SensorDataDto>> getAllSensorDataBySensor5Min(@PathVariable String sensorId) {
        return ResponseEntity.ok(sensorDataService.getAllSensorDataBySensorId5Min(sensorId));
    }
    @GetMapping("/SensorData/last10m-by-sensorId/{sensorId}")
    public ResponseEntity<List<SensorDataDto>> getAllSensorDataBySensor10Min(@PathVariable String sensorId) {
        return ResponseEntity.ok(sensorDataService.getAllSensorDataBySensorId10Min(sensorId));
    }
    @GetMapping("/SensorData/last15m-by-sensorId/{sensorId}")
    public ResponseEntity<List<SensorDataDto>> getAllSensorDataBySensor15Min(@PathVariable String sensorId) {
        return ResponseEntity.ok(sensorDataService.getAllSensorDataBySensorId15Min(sensorId));
    }








    @GetMapping("/SensorData/public/last5m")
    public ResponseEntity<List<SensorDataDto>> getAllSensorDataIn5Min() {
        return ResponseEntity.ok(sensorDataService.getAllPublicSensorDataIn5Min());
    }
    @GetMapping("/SensorData/public/last10m")
    public ResponseEntity<List<SensorDataDto>> getAllSensorDataIn10Min() {
        return ResponseEntity.ok(sensorDataService.getAllPublicSensorDataIn10Min());
    }
    @GetMapping("/SensorData/public/last15m")
    public ResponseEntity<List<SensorDataDto>> getAllSensorDataIn15Min() {
        return ResponseEntity.ok(sensorDataService.getAllPublicSensorDataIn15Min());
    }

   /* @PostMapping("/SensorData/date")
    public ResponseEntity<List<SensorDataDto>> getAllSensorDataBetweenDate(@RequestBody DateDto date) {
        return ResponseEntity.ok(sensorDataService.getAllSensorDataBetweenDate(date));
    }*/

    @PostMapping("/SensorData/date/sensor")
    public ResponseEntity<List<SensorDataDto>> getAllSensorDataBySensorBetweenDate(@RequestBody DateDto date) {
        return ResponseEntity.ok(sensorDataService.getAllSensorDataBySensorBetweenDate(date));
    }

    /*
    @GetMapping("/SensorData/{id}")
    public ResponseEntity<SensorDataDto> getSensorDataById(@PathVariable String id) {
        return ResponseEntity.ok(sensorDataService.getSensorDataById(id));

    }*/

    @GetMapping("/SensorData/Sensor/{id}/{token}")
    public ResponseEntity<SensorDataDto> getSensorDataBySensorId(@PathVariable String id, @PathVariable String token ) {
        return ResponseEntity.ok(sensorDataService.getLatestSensorDataBySensorId(token,id));
    }
    @PutMapping("/SensorData/{id}")
    public ResponseEntity<SensorData> updateSensorData(@RequestBody NewSensorDataDto newSensorDataDto) {
        return ResponseEntity.ok(sensorDataService.update(newSensorDataDto));
    }
    @DeleteMapping("/SensorData/{id}/{token}")
    public HttpStatus deleteSensorData(@PathVariable String id, @PathVariable String token) {
        sensorDataService.delete(token,id);
        return HttpStatus.OK;
    }
    @GetMapping("/SensorData/area/{interestAreaId}/{token}")
    public ResponseEntity<SensorDataInterestAreaDto> getSensorDataByInterestArea(@PathVariable String interestAreaId , @PathVariable String token ) {
        return ResponseEntity.ok( sensorDataService.getAllSensorDataProcessedByInterestArea( interestAreaId, token));
    }
    @GetMapping("/SensorData/public/processed/{type}")
    public ResponseEntity<String> getProcessedDataByType(@PathVariable String type ) {
        return ResponseEntity.ok( sensorDataService.getProcessedSensorData(type));
    }
}
