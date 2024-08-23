package com.myTesi.aloisioUmberto.controller;

import com.myTesi.aloisioUmberto.data.entities.InterestArea;
import com.myTesi.aloisioUmberto.data.services.interfaces.InterestAreaService;
import com.myTesi.aloisioUmberto.dto.InterestAreaDto;
import com.myTesi.aloisioUmberto.dto.New.NewInterestAreaDto;
import com.myTesi.aloisioUmberto.dto.SensorDataDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://192.168.15.34:4200")
@Tag(name = "InterestArea") //Name displayed on swagger
public class InterestAreaController {

    private final InterestAreaService interestAreaService;

    @PostMapping(value = "/interestarea", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<InterestAreaDto> createInterestArea(@RequestPart("data") NewInterestAreaDto data, @RequestPart(value = "file" , required = false) MultipartFile file) throws IOException {
        InterestAreaDto savedInterestArea = interestAreaService.save(data, file);
        return ResponseEntity.ok(savedInterestArea);
    }


    @GetMapping("/{interestAreaId}/latest-sensor-data")
    public ResponseEntity<List<SensorDataDto>> getLatestSensorDataInInterestArea(@PathVariable String interestAreaId) {
        List<SensorDataDto> sensorDataList = interestAreaService.getLatestSensorDataInInterestArea(interestAreaId);
        if (sensorDataList != null && !sensorDataList.isEmpty()) {
            return ResponseEntity.ok(sensorDataList);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }
    }


    @GetMapping("/interestarea/{id}")
    public ResponseEntity<InterestArea> getInterestArea(@PathVariable String id) {
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