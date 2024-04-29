package com.myTesi.aloisioUmberto.controller;

import com.myTesi.aloisioUmberto.data.entities.InterestArea;
import com.myTesi.aloisioUmberto.data.services.interfaces.InterestAreaService;
import com.myTesi.aloisioUmberto.dto.New.NewInterestAreaDto;
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


    @PostMapping
    public ResponseEntity<InterestArea> createInterestArea(@RequestBody NewInterestAreaDto request) {
        return ResponseEntity.ok( interestAreaService.save(request));
    }


    @GetMapping("/interestarea/{id}")
    public ResponseEntity<InterestArea> getInterestArea(@PathVariable ObjectId id) {
        //TODO ADD TOKEN CHECK
        return ResponseEntity.ok(interestAreaService.getInterestArea(id));
    }

    @GetMapping("/interestarea")
    public ResponseEntity<List<InterestArea>> getInterestAreasByUserId(@RequestParam ObjectId userId) {
        //TODO ADD TOKEN CHECK
        List<InterestArea> interestAreas = interestAreaService.getInterestAreasByUserId(userId);
        return ResponseEntity.ok(interestAreas);
    }

    @DeleteMapping("/interestarea/{id}")
    public ResponseEntity<Void> deleteInterestArea(@PathVariable ObjectId id) {
        //TODO ADD TOKEN CHECK
        interestAreaService.deleteInterestArea(id);
        return ResponseEntity.noContent().build();
    }
}