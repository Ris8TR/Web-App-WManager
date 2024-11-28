package com.myTesi.aloisioUmberto.controller;

import com.myTesi.aloisioUmberto.data.entities.SensorData;
import com.myTesi.aloisioUmberto.data.services.SensorServiceImpl;
import com.myTesi.aloisioUmberto.data.services.interfaces.SensorService;
import com.myTesi.aloisioUmberto.dto.New.NewSensorDataDto;
import com.myTesi.aloisioUmberto.dto.New.NewSensorDto;
import com.myTesi.aloisioUmberto.dto.New.NewUserDto;
import com.myTesi.aloisioUmberto.dto.SensorAndAreas;
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


    //CREATE & UPDATE
    @PostMapping("/sensors")
    public ResponseEntity<SensorDto> addSensor(@RequestBody @Valid NewSensorDto newSensorDto) {
        return ResponseEntity.ok(sensorService.saveDto(newSensorDto));
    }

    @Operation(summary = "Save sensor ", description = "Save sensor with file")
    @PostMapping(value="/new-sensors",  consumes = {"multipart/form-data"})
    public ResponseEntity<SensorDto> newSensor( @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
        return ResponseEntity.ok(sensorService.save(file));
    }

    @PutMapping("/sensors/update")
    public ResponseEntity<SensorDto> updateSensor(@RequestBody SensorDto sensorDto) {
        return ResponseEntity.ok(sensorService.update(sensorDto));
    }




    //PUBLIC
    @GetMapping("/sensors/public/all-sensors")
    public ResponseEntity<List<SensorDto>> getAllSensor() {
        return ResponseEntity.ok(sensorService.getAllSensor());
    }

    @GetMapping("/sensors/public/company/{companyName}")
    public ResponseEntity <List<SensorDto>> findPublicByCompanyName(@PathVariable @Valid String companyName) {
        return ResponseEntity.ok(sensorService.findPublicByCompanyName(companyName));
    }

    @GetMapping("/sensors/public/{id}")
    public ResponseEntity <Optional<SensorDto>> findPublicById(@PathVariable @Valid String id) {
        return ResponseEntity.ok(sensorService.findPublicById(id));
    }

    @GetMapping("/sensors/public/type/{type}/")
    public ResponseEntity <List<SensorDto>> findPublicByTypeAndUserId(@PathVariable String type) {
        return ResponseEntity.ok(sensorService.findPublicByType(type));
    }





    //PRIVATE
    @GetMapping("/sensors/company/{companyName}/{token}")
    public ResponseEntity <List<SensorDto>> findByCompanyName(@PathVariable @Valid String companyName,  @PathVariable @Valid String token) {
        return ResponseEntity.ok(sensorService.findByCompanyName(companyName, token));
    }

    @GetMapping("/sensors/interestArea/{interestAreaId}/{token}")
    public ResponseEntity <List<SensorDto>> findByInterestAreaId(@PathVariable @Valid String interestAreaId,  @PathVariable @Valid String token) {
        return ResponseEntity.ok(sensorService.findByInterestAreaId(interestAreaId, token));
    }

    @GetMapping("/sensors/{id}")
    public ResponseEntity <Optional<SensorDto>> findById(@PathVariable @Valid String id,  @PathVariable @Valid String token) {
        return ResponseEntity.ok(sensorService.findById(id, token));
    }

    @GetMapping("/sensors/type/{type}/{token}")
    public ResponseEntity <List<SensorDto>> findByTypeAndUserId(@PathVariable String type, String token) {
        return ResponseEntity.ok(sensorService.findByTypeAndUser(type, token));
    }

    @GetMapping("/sensors/user/{token}")
    public ResponseEntity <List<SensorDto>> findByUserId(@PathVariable String token) {
        return ResponseEntity.ok(sensorService.findByUserId(token));
    }

    @GetMapping("/sensors/area/user/{token}")
    public ResponseEntity <SensorAndAreas> findAreaByUserId(@PathVariable String token) {
        return ResponseEntity.ok(sensorService.findAndAreaByUserId(token));
    }




}
