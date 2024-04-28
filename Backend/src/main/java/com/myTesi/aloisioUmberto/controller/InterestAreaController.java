package com.myTesi.aloisioUmberto.controller;

import com.myTesi.aloisioUmberto.data.entities.InterestArea;
import com.myTesi.aloisioUmberto.data.services.interfaces.InterestAreaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "InterestArea") //Name displayed on swagger
public class InterestAreaController {

    private final InterestAreaService interestAreaService;


        /*
    @PostMapping
    public ResponseEntity<InterestArea> createInterestArea(@RequestBody CreateInterestAreaRequest request) {
        InterestArea interestArea = interestAreaService.createInterestArea(request.getUserId(), request.getName(), request.getGeometry());
        return ResponseEntity.ok(interestArea);
    }
        */

    @GetMapping("/interestarea/{id}")
    public ResponseEntity<InterestArea> getInterestArea(@PathVariable ObjectId id) {
        InterestArea interestArea = interestAreaService.getInterestArea(id);
        if (interestArea == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(interestArea);
    }

    @GetMapping("/interestarea")
    public ResponseEntity<List<InterestArea>> getInterestAreasByUserId(@RequestParam ObjectId userId) {
        List<InterestArea> interestAreas = interestAreaService.getInterestAreasByUserId(userId);
        return ResponseEntity.ok(interestAreas);
    }

    @DeleteMapping("/interestarea/{id}")
    public ResponseEntity<Void> deleteInterestArea(@PathVariable ObjectId id) {
        interestAreaService.deleteInterestArea(id);
        return ResponseEntity.noContent().build();
    }
}