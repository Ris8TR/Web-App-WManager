package com.myTesi.aloisioUmberto.controller;

import com.myTesi.aloisioUmberto.data.services.SensorServiceImpl;
import com.myTesi.aloisioUmberto.data.services.interfaces.SensorService;
import com.myTesi.aloisioUmberto.dto.New.NewSensorDto;
import com.myTesi.aloisioUmberto.dto.New.NewUserDto;
import com.myTesi.aloisioUmberto.dto.SensorDto;
import com.myTesi.aloisioUmberto.dto.UserDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
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


    @GetMapping("/sensors/{companyName}")
    public ResponseEntity <List<SensorDto>> findByCompanyName(@PathVariable @Valid String companyName) {
        return ResponseEntity.ok(sensorService.findByCompanyName(companyName));
    }

    @GetMapping("/sensors/{id}")
    public ResponseEntity <Optional<SensorDto>> findById(@PathVariable @Valid String id) {
        return ResponseEntity.ok(sensorService.findById(id));
    }




}
