package com.myTesi.aloisioUmberto.controller;

import com.myTesi.aloisioUmberto.data.entities.InterestArea;
import com.myTesi.aloisioUmberto.data.services.interfaces.InterestAreaService;
import com.myTesi.aloisioUmberto.dto.InterestAreaDto;
import com.myTesi.aloisioUmberto.dto.New.NewInterestAreaDto;
import com.myTesi.aloisioUmberto.dto.SensorDataDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "InterestArea") //Name displayed on swagger
public class InterestAreaController {

    private final InterestAreaService interestAreaService;


    @PostMapping
    public ResponseEntity<InterestAreaDto> createInterestArea(@RequestBody NewInterestAreaDto request) {
        return ResponseEntity.ok( interestAreaService.save(request));
    }


    @GetMapping("/{interestAreaId}/latest-sensor-data")
    public ResponseEntity<List<SensorDataDto>> getLatestSensorDataInInterestArea(@PathVariable ObjectId interestAreaId) {
        List<SensorDataDto> sensorDataList = interestAreaService.getLatestSensorDataInInterestArea(interestAreaId);
        if (sensorDataList != null && !sensorDataList.isEmpty()) {
            return ResponseEntity.ok(sensorDataList);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }
    }


    @GetMapping("/interestarea/{id}")
    public ResponseEntity<InterestArea> getInterestArea(@PathVariable ObjectId id) {
        //TODO ADD TOKEN CHECK
        return ResponseEntity.ok(interestAreaService.getInterestArea(id));
    }

    @GetMapping("/interestarea")
    public ResponseEntity<List<InterestAreaDto>> getInterestAreasByUser(@RequestParam String token) {
        //TODO ADD TOKEN CHECK
        List<InterestAreaDto> interestAreas = interestAreaService.getInterestAreasByUserId(token);
        return ResponseEntity.ok(interestAreas);
    }

    @DeleteMapping("/interestarea/{id}")
    public ResponseEntity<Void> deleteInterestArea(@PathVariable ObjectId id) {
        //TODO ADD TOKEN CHECK
        interestAreaService.deleteInterestArea(id);
        return ResponseEntity.noContent().build();
    }
}