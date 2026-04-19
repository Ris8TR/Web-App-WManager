package com.myTesi.aloisioUmberto.controller;

import com.myTesi.aloisioUmberto.config.JwtTokenProvider;
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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.opengis.filter.identity.ObjectId;
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
    private final JwtTokenProvider jwtTokenProvider;



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

    @SecurityRequirement(name="Bearer Authentication")
    @PutMapping("/sensors/update")
    public ResponseEntity<SensorDto> updateSensor(HttpServletRequest request , @RequestBody SensorDto sensorDto) {
        return ResponseEntity.ok(sensorService.update(sensorDto));
    }

    @SecurityRequirement(name="Bearer Authentication")
    @DeleteMapping("/sensors/{id}")
    public ResponseEntity<Void> deleteSensor(HttpServletRequest request , @PathVariable ObjectId id) {
        String token = jwtTokenProvider.getTokenFromRequest(request);
        sensorService.deleteSensorById(id, token);
        return ResponseEntity.noContent().build();
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
    @SecurityRequirement(name="Bearer Authentication")
    @GetMapping("/sensors/company/{companyName}")
    public ResponseEntity <List<SensorDto>> findByCompanyName(HttpServletRequest request ,@PathVariable @Valid String companyName) {
        String token = jwtTokenProvider.getTokenFromRequest(request);
        return ResponseEntity.ok(sensorService.findByCompanyName(companyName, token));
    }

    @SecurityRequirement(name="Bearer Authentication")
    @GetMapping("/sensors/interestArea/{interestAreaId}")
    public ResponseEntity <List<SensorDto>> findByInterestAreaId(HttpServletRequest request ,@PathVariable @Valid String interestAreaId) {
        String token = jwtTokenProvider.getTokenFromRequest(request);
        return ResponseEntity.ok(sensorService.findByInterestAreaId(interestAreaId, token));
    }

    @SecurityRequirement(name="Bearer Authentication")
    @GetMapping("/sensors/{id}/")
    public ResponseEntity <Optional<SensorDto>> findById(HttpServletRequest request ,@PathVariable @Valid String id) {
        String token = jwtTokenProvider.getTokenFromRequest(request);
        return ResponseEntity.ok(sensorService.findById(id, token));
    }

    @SecurityRequirement(name="Bearer Authentication")
    @GetMapping("/sensors/type/{type}/")
    public ResponseEntity <List<SensorDto>> findByTypeAndUserId(HttpServletRequest request ,@PathVariable String type) {
        String token = jwtTokenProvider.getTokenFromRequest(request);
        return ResponseEntity.ok(sensorService.findByTypeAndUser(type, token));
    }

    @SecurityRequirement(name="Bearer Authentication")
    @GetMapping("/sensors/user")
    public ResponseEntity <List<SensorDto>> findByUserId(HttpServletRequest request ) {
        String token = jwtTokenProvider.getTokenFromRequest(request);
        return ResponseEntity.ok(sensorService.findByUserId(token));
    }

    @SecurityRequirement(name="Bearer Authentication")
    @GetMapping("/sensors/area")
    public ResponseEntity <SensorAndAreas> findAreaByUserId(HttpServletRequest request ) {
        String token = jwtTokenProvider.getTokenFromRequest(request);
        return ResponseEntity.ok(sensorService.findAndAreaByUserId(token));
    }




}
