package com.myTesi.aloisioUmberto.controller;
import com.myTesi.aloisioUmberto.data.services.interfaces.UserPreferenceService;
import com.myTesi.aloisioUmberto.dto.New.NewUserPreferenceDto;
import com.myTesi.aloisioUmberto.dto.UserPreferenceDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "UserPreference") //Name displayed on swagger
public class UserPreferenceController {

    private final UserPreferenceService userPreferenceService;


    @PostMapping("/UserPreference")
    public ResponseEntity<UserPreferenceDto> saveUserPreference(@RequestBody NewUserPreferenceDto newUserPreferenceDTO) {
        //TODO ADD TOKEN CHECK
        return ResponseEntity.ok(userPreferenceService.save(newUserPreferenceDTO));
    }

    @GetMapping("/UserPreference/getAll")
    public ResponseEntity<List<UserPreferenceDto>> getAllUserPreferences() {
        //TODO ADD TOKEN CHECK
        return ResponseEntity.ok(userPreferenceService.getAllUserPreferences());
    }

    @GetMapping("/UserPreference/{userId}")
    public ResponseEntity<UserPreferenceDto> getUserPreferenceByUserId(@PathVariable String userId) {
        //TODO ADD TOKEN CHECK
        return ResponseEntity.ok(userPreferenceService.getUserPreferenceByUserId(userId));
    }

    @PutMapping("/UserPreference/{userId}")
    public ResponseEntity<UserPreferenceDto> updateUserPreference(@RequestBody UserPreferenceDto userPreferenceDTO) {
        //TODO ADD TOKEN CHECK
        return ResponseEntity.ok( userPreferenceService.update(userPreferenceDTO));
    }

    @DeleteMapping("/UserPreference/{userId}")
    public ResponseEntity<Void> deleteUserPreference(@PathVariable String userId) {
        //TODO ADD TOKEN CHECK
        userPreferenceService.delete(userId);
        return ResponseEntity.noContent().build();
    }
}
