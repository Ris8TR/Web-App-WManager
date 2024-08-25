package com.myTesi.aloisioUmberto.controller;

import com.myTesi.aloisioUmberto.data.entities.SensorData;
import com.myTesi.aloisioUmberto.data.services.SensorServiceImpl;
import com.myTesi.aloisioUmberto.data.services.interfaces.SensorService;
import com.myTesi.aloisioUmberto.dto.New.NewSensorDataDto;
import com.myTesi.aloisioUmberto.dto.New.NewSensorDto;
import com.myTesi.aloisioUmberto.dto.New.NewUserDto;
import com.myTesi.aloisioUmberto.dto.SensorDto;
import com.myTesi.aloisioUmberto.dto.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://192.168.15.34:4200")
@Tag(name = "Sensor") //Name displayed on swagger

public class SensorController {

    private final SensorService sensorService;

    @GetMapping("/sensors/all-sensors")
    public ResponseEntity<List<SensorDto>> getAllSensor() {
        return ResponseEntity.ok(sensorService.getAllSensor());
    }

    @PostMapping("/sensors")
    public ResponseEntity<SensorDto> addSensor(@RequestBody @Valid NewSensorDto newSensorDto) {
        return ResponseEntity.ok(sensorService.saveDto(newSensorDto));
    }

    @Operation(summary = "Save sensor ", description = "Save sensor with file")
    @PostMapping(value="/new-sensors",  consumes = {"multipart/form-data"})
    public ResponseEntity<SensorDto> newSensor( @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
        return ResponseEntity.ok(sensorService.save(file));
    }



    @GetMapping("/sensors/{companyName}/{token}")
    public ResponseEntity <List<SensorDto>> findByCompanyName(@PathVariable @Valid String companyName, @Valid String token) {
        return ResponseEntity.ok(sensorService.findByCompanyName(companyName, token));
    }

    @GetMapping("/sensors/{id}/{token}")
    public ResponseEntity <Optional<SensorDto>> findById(@PathVariable @Valid String id, @Valid String token) {
        return ResponseEntity.ok(sensorService.findById(id, token));
    }

    @GetMapping("/sensors/user/{token}")
    public ResponseEntity <List<SensorDto>> findByUserId(@PathVariable String token) {
        return ResponseEntity.ok(sensorService.findByUserId(token));
    }




}
