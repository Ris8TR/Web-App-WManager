package com.myTesi.aloisioUmberto.controller;
import com.myTesi.aloisioUmberto.data.entities.SensorData;
import com.myTesi.aloisioUmberto.data.services.interfaces.SensorDataService;
import com.myTesi.aloisioUmberto.dto.New.NewSensorDataDto;
import com.myTesi.aloisioUmberto.dto.SensorDataDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<SensorData> saveSensorData(@RequestBody NewSensorDataDto newSensorDataDTO) {
        //TODO ADD TOKEN CHECK
        return ResponseEntity.ok(sensorDataService.save(newSensorDataDTO));
    }


    @GetMapping("/SensorData/get-all")
    public ResponseEntity<List<SensorDataDto>> getAllSensorData() {
        return ResponseEntity.ok(sensorDataService.getAllSensorData());
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
}
