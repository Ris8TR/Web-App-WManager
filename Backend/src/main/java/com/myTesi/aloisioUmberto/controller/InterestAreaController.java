package com.myTesi.aloisioUmberto.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myTesi.aloisioUmberto.config.JwtTokenProvider;
import com.myTesi.aloisioUmberto.data.entities.InterestArea;
import com.myTesi.aloisioUmberto.data.services.interfaces.InterestAreaService;
import com.myTesi.aloisioUmberto.dto.InterestAreaDto;
import com.myTesi.aloisioUmberto.dto.New.NewInterestAreaDto;
import com.myTesi.aloisioUmberto.dto.SensorDataDto;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final JwtTokenProvider jwtTokenProvider;

    //CREAZIONE AREA
    @PostMapping(value = "/interestArea", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<InterestAreaDto> createInterestArea(HttpServletRequest request ,@RequestPart("data") NewInterestAreaDto data, @RequestPart(value = "file" , required = false) MultipartFile file) throws IOException {
        InterestAreaDto savedInterestArea = interestAreaService.save(data, file);
        return ResponseEntity.ok(savedInterestArea);
    }

    //PUBLIC
    @GetMapping("/interestArea/public")
    public ResponseEntity<List<InterestAreaDto>> getAllPublicInterestArea() {
        return ResponseEntity.ok(interestAreaService.getAllPublicInterestArea());
    }

    //ADMIN
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/interestArea/admin/getAll")
    public ResponseEntity<List<InterestAreaDto>> getAllInterestAreaAdmin() {
        return ResponseEntity.ok(interestAreaService.getAllPublicInterestArea());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name="Bearer Authentication")
    @GetMapping("/interestArea/admin/getUserAreas/{id}")
    public ResponseEntity<List<InterestAreaDto>> getAllInterestAreasByUserAdmin(HttpServletRequest request, @PathVariable String id) {
        List<InterestAreaDto> interestAreas = interestAreaService.getInterestAreasByUserIdAdmin(id);
        return ResponseEntity.ok(interestAreas);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name="Bearer Authentication")
    @GetMapping("/interestArea/admin/getUserArea/{id}")
    public ResponseEntity<InterestAreaDto> getInterestAreasByUserAdmin(HttpServletRequest request, @PathVariable String id) {
        InterestAreaDto interestArea = interestAreaService.getInterestAreaAdmin(id);
        return ResponseEntity.ok(interestArea);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/interestArea/admin/update")
    public ResponseEntity<InterestAreaDto> updateInterestAreaAdmin(HttpServletRequest request, @RequestPart(value = "data") InterestAreaDto data,@RequestPart(value = "geometry", required = false) MultipartFile geometry, @RequestPart(value = "preview", required = false) MultipartFile preview) throws IOException {
        return ResponseEntity.ok(interestAreaService.updateAdmin(data, geometry, preview));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name="Bearer Authentication")
    @DeleteMapping("/interestArea/admin/{id}")
    public ResponseEntity<Void> deleteInterestAreaAdmin( @PathVariable ObjectId id) {
        interestAreaService.deleteInterestAreaAdmin(id);
        return ResponseEntity.noContent().build();
    }


    //PRIVATE
    @SecurityRequirement(name="Bearer Authentication")
    @GetMapping("/interestArea/{interestAreaId}/latest-sensor-data/{token}")
    public ResponseEntity<List<SensorDataDto>> getLatestSensorDataInInterestArea(HttpServletRequest request ,@PathVariable String interestAreaId, @PathVariable String token) {
        List<SensorDataDto> sensorDataList = interestAreaService.getLatestSensorDataInInterestArea(interestAreaId, token);
        if (sensorDataList != null && !sensorDataList.isEmpty()) {
            return ResponseEntity.ok(sensorDataList);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }
    }

    @SecurityRequirement(name="Bearer Authentication")
    @GetMapping("/interestArea/{id}")
    public ResponseEntity<InterestArea> getInterestArea(HttpServletRequest request , @PathVariable String id) {
        String token = jwtTokenProvider.getTokenFromRequest(request);
        return ResponseEntity.ok(interestAreaService.getInterestArea(id, token));
    }

    @SecurityRequirement(name="Bearer Authentication")
    @GetMapping("/interestArea")
    public ResponseEntity<List<InterestAreaDto>> getInterestAreasByUser(HttpServletRequest request ) {
        String token = jwtTokenProvider.getTokenFromRequest(request);
        List<InterestAreaDto> interestAreas = interestAreaService.getInterestAreasByUserId(token);
        return ResponseEntity.ok(interestAreas);
    }


    @PutMapping("/interestArea/update")
    public ResponseEntity<InterestAreaDto> updateInterestArea(@RequestPart(value = "data") String dataJson,@RequestPart(value = "geometry", required = false) MultipartFile geometry,@RequestPart(value = "preview", required = false) MultipartFile preview) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        InterestAreaDto data = objectMapper.readValue(dataJson, InterestAreaDto.class);
        return ResponseEntity.ok(interestAreaService.update(data, geometry, preview));
    }

    @SecurityRequirement(name="Bearer Authentication")
    @DeleteMapping("/interestArea/{id}")
    public ResponseEntity<Void> deleteInterestArea(HttpServletRequest request , @PathVariable ObjectId id) {
        String token = jwtTokenProvider.getTokenFromRequest(request);
        interestAreaService.deleteInterestArea(id, token);
        return ResponseEntity.noContent().build();
    }
}