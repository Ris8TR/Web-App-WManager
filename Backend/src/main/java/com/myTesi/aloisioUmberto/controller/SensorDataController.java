package com.myTesi.aloisioUmberto.controller;
import com.myTesi.aloisioUmberto.config.JwtTokenProvider;
import com.myTesi.aloisioUmberto.data.entities.SensorData;
import com.myTesi.aloisioUmberto.data.services.interfaces.SensorDataService;
import com.myTesi.aloisioUmberto.dto.DateDto;
import com.myTesi.aloisioUmberto.dto.New.NewSensorDataDto;
import com.myTesi.aloisioUmberto.dto.SensorDataDto;
import com.myTesi.aloisioUmberto.dto.SensorDataInterestAreaDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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
    private final JwtTokenProvider jwtTokenProvider;


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


    @SecurityRequirement(name="Bearer Authentication")
    @GetMapping("/SensorData/private/top-by-interestAreaId/{interestAreaId}")
    public ResponseEntity<SensorDataInterestAreaDto> getLastPrivateSensorDataByInterestAreaId(HttpServletRequest request , @PathVariable String interestAreaId) {
        String token = jwtTokenProvider.getTokenFromRequest(request);
        return ResponseEntity.ok(sensorDataService.getTopSensorDataByInterestAreaId(interestAreaId, token));
    }

    @SecurityRequirement(name="Bearer Authentication")
    @GetMapping("/SensorData/private/top-by-sensor-interestAreaId/{interestAreaId}/{sensorId}")
    public ResponseEntity<SensorDataInterestAreaDto> getLastPrivateSensorDataBySensorAndInterestAreaId(HttpServletRequest request ,@PathVariable String interestAreaId, @PathVariable String sensorId) {
        String token = jwtTokenProvider.getTokenFromRequest(request);
        return ResponseEntity.ok(sensorDataService.getTopSensorDataByInterestAreaIdAndSensorId(interestAreaId, sensorId, token));
    }

    @SecurityRequirement(name="Bearer Authentication")
    @GetMapping("/SensorData/private/top-by-sensorId/{sensorId}")
    public ResponseEntity<SensorDataInterestAreaDto> getLastPrivateSensorDataBySensor(HttpServletRequest request ,@PathVariable String sensorId) {
        String token = jwtTokenProvider.getTokenFromRequest(request);
        return ResponseEntity.ok(sensorDataService.getTopSensorDataBySensorId(sensorId, token));
    }




    @SecurityRequirement(name="Bearer Authentication")
    @GetMapping("/SensorData/private/last/5/interestAreaId/{interestAreaId}")
    public ResponseEntity<SensorDataInterestAreaDto> getAllPrivateSensorDataByInterestAreaId5Min(HttpServletRequest request ,@PathVariable String interestAreaId) {
        String token = jwtTokenProvider.getTokenFromRequest(request);
        return ResponseEntity.ok(sensorDataService.getAllSensorDataByInterestAreaId5Min(interestAreaId, token));
    }

    @GetMapping("/SensorData/private/last/10m/interestAreaId/{interestAreaId}")
    public ResponseEntity<SensorDataInterestAreaDto> getAllPrivateSensorDataByInterestAreaId10Min(HttpServletRequest request ,@PathVariable String interestAreaId) {
        String token = jwtTokenProvider.getTokenFromRequest(request);
        return ResponseEntity.ok(sensorDataService.getAllSensorDataByInterestAreaId10Min(interestAreaId, token));
    }

    @SecurityRequirement(name="Bearer Authentication")
    @GetMapping("/SensorData/private/last/15/interestAreaId/{interestAreaId}")
    public ResponseEntity<SensorDataInterestAreaDto> getAllPrivateSensorDataByInterestAreaId15Min(HttpServletRequest request ,@PathVariable String interestAreaId) {
        String token = jwtTokenProvider.getTokenFromRequest(request);
        return ResponseEntity.ok(sensorDataService.getAllSensorDataByInterestAreaId15Min(interestAreaId, token));
    }

    @SecurityRequirement(name="Bearer Authentication")
    @GetMapping("/SensorData/private/last/5/sensor/{sensorId}/interestAreaId/{interestAreaId}")
    public ResponseEntity<SensorDataInterestAreaDto> getAllPrivateSensorDataBySensorAndInterestAreaId5Min(HttpServletRequest request ,@PathVariable String interestAreaId, @PathVariable String sensorId) {
        String token = jwtTokenProvider.getTokenFromRequest(request);
        return ResponseEntity.ok(sensorDataService.getAllSensorDataByInterestAreaIdAndSensorId5Min(interestAreaId,sensorId));
    }

    @SecurityRequirement(name="Bearer Authentication")
    @GetMapping("/SensorData/private/last/10/sensor/{sensorId}/interestAreaId/{interestAreaId}")
    public ResponseEntity<SensorDataInterestAreaDto> getAllPrivateSensorDataBySensorAndInterestAreaId10Min(HttpServletRequest request ,@PathVariable String interestAreaId, @PathVariable String sensorId) {
        String token = jwtTokenProvider.getTokenFromRequest(request);
        return ResponseEntity.ok(sensorDataService.getAllSensorDataByInterestAreaIdAndSensorId5Min(interestAreaId,sensorId));
    }

    @SecurityRequirement(name="Bearer Authentication")
    @GetMapping("/SensorData/private/last/15/sensor/{sensorId}/interestAreaId/{interestAreaId}")
    public ResponseEntity<SensorDataInterestAreaDto> getAllPrivateSensorDataBySensorAndInterestAreaId15Min(HttpServletRequest request ,@PathVariable String interestAreaId, @PathVariable String sensorId) {
        String token = jwtTokenProvider.getTokenFromRequest(request);
        return ResponseEntity.ok(sensorDataService.getAllSensorDataByInterestAreaIdAndSensorId5Min(interestAreaId,sensorId));
    }

    @SecurityRequirement(name="Bearer Authentication")
    @GetMapping("/SensorData/private/last/5/sensorId/{sensorId}")
    public ResponseEntity<SensorDataInterestAreaDto> getAllPrivateSensorDataBySensor5Min(HttpServletRequest request ,@PathVariable String sensorId) {
        String token = jwtTokenProvider.getTokenFromRequest(request);
        return ResponseEntity.ok(sensorDataService.getAllSensorDataBySensorId5Min(sensorId, token));
    }

    @SecurityRequirement(name="Bearer Authentication")
    @GetMapping("/SensorData/private/last/10/sensorId/{sensorId}")
    public ResponseEntity<SensorDataInterestAreaDto> getAllPrivateSensorDataBySensor10Min(HttpServletRequest request ,@PathVariable String sensorId) {
        String token = jwtTokenProvider.getTokenFromRequest(request);
        return ResponseEntity.ok(sensorDataService.getAllSensorDataBySensorId10Min(sensorId, token));
    }

    @SecurityRequirement(name="Bearer Authentication")
    @GetMapping("/SensorData/private/last/15/sensorId/{sensorId}")
    public ResponseEntity<SensorDataInterestAreaDto> getAllPrivateSensorDataBySensor15Min(HttpServletRequest request ,@PathVariable String sensorId) {
        String token = jwtTokenProvider.getTokenFromRequest(request);
        return ResponseEntity.ok(sensorDataService.getAllSensorDataBySensorId15Min(sensorId,token));
    }


    //TODO
    @SecurityRequirement(name="Bearer Authentication")
    @PostMapping("/SensorData/private/date/sensor")
    public ResponseEntity<SensorDataInterestAreaDto> getAllPrivateSensorDataBySensorBetweenDate(HttpServletRequest request ,@RequestBody DateDto date) {
        String token = jwtTokenProvider.getTokenFromRequest(request);
        return ResponseEntity.ok(sensorDataService.getAllSensorDataBySensorBetweenDate(date));
    }






    @GetMapping("/SensorData/public/last/5")
    public ResponseEntity<SensorDataInterestAreaDto> getAllPublicSensorDataIn5Min() {
        return ResponseEntity.ok(sensorDataService.getAllPublicSensorDataIn5Min());
    }

    @GetMapping("/SensorData/public/last/10")
    public ResponseEntity<SensorDataInterestAreaDto> getAllPublicSensorDataIn10Min() {
        return ResponseEntity.ok(sensorDataService.getAllPublicSensorDataIn10Min());
    }

    @GetMapping("/SensorData/public/last/15")
    public ResponseEntity<SensorDataInterestAreaDto> getAllPublicSensorDataIn15Min() {
        return ResponseEntity.ok(sensorDataService.getAllPublicSensorDataIn15Min());
    }

    @PostMapping("/SensorData/public/top")
    public ResponseEntity<SensorDataInterestAreaDto> getAllPublicSensorDataOnTop() {
        return ResponseEntity.ok(sensorDataService.getTopPublicSensorData());
    }
    @SecurityRequirement(name="Bearer Authentication")
    @GetMapping("/SensorData/public/top-by-interestAreaId/{interestAreaId}")
    public ResponseEntity<SensorDataInterestAreaDto> getLastPublicSensorDataByInterestAreaId(@PathVariable String interestAreaId) {
        return ResponseEntity.ok(sensorDataService.getTopPublicSensorDataByInterestAreaId(interestAreaId));
    }


    /*
    @GetMapping("/SensorData/{id}")
    public ResponseEntity<SensorDataDto> getSensorDataById(@PathVariable String id) {
        return ResponseEntity.ok(sensorDataService.getSensorDataById(id));

    }*/

    @GetMapping("/SensorData/private/Sensor/{id}")
    public ResponseEntity<SensorDataDto> getPrivateSensorDataBySensorId(HttpServletRequest request , @PathVariable String id ) {
        String token = jwtTokenProvider.getTokenFromRequest(request);
        return ResponseEntity.ok(sensorDataService.getLatestSensorDataBySensorId(token,id));
    }
    @PutMapping("/SensorData/private")
    public ResponseEntity<SensorData> updateSensorData(HttpServletRequest request, @RequestBody NewSensorDataDto newSensorDataDto) {
        String token = jwtTokenProvider.getTokenFromRequest(request);
        return ResponseEntity.ok(sensorDataService.update(newSensorDataDto));
    }
    @DeleteMapping("/SensorData/private/{id}")
    public HttpStatus deleteSensorData(HttpServletRequest request, @PathVariable String id) {
        String token = jwtTokenProvider.getTokenFromRequest(request);
        sensorDataService.delete(token,id);
        return HttpStatus.OK;
    }
    @GetMapping("/SensorData/private/area/{interestAreaId}")
    public ResponseEntity<SensorDataInterestAreaDto> getPrivateSensorDataByInterestArea(HttpServletRequest request, @PathVariable String interestAreaId  ) {
        String token = jwtTokenProvider.getTokenFromRequest(request);
        return ResponseEntity.ok( sensorDataService.getAllSensorDataProcessedByInterestArea( interestAreaId, token));
    }
    @GetMapping("/SensorData/public/processed/{type}")
    public ResponseEntity<String> getPublicProcessedDataByType(HttpServletRequest request, @PathVariable String type ) {
        String token = jwtTokenProvider.getTokenFromRequest(request);
        return ResponseEntity.ok( sensorDataService.getProcessedSensorData(type));
    }
}
