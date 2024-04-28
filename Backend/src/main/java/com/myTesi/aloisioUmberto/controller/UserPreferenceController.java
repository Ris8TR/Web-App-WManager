package com.myTesi.aloisioUmberto.controller;
import com.myTesi.aloisioUmberto.data.entities.UserPreference;
import com.myTesi.aloisioUmberto.data.services.interfaces.UserPreferenceService;
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
@Tag(name = "UserPreference") //Name displayed on swagger
public class UserPreferenceController {

    private final UserPreferenceService userPreferenceService;


    @PostMapping("/UserPreference")
    public ResponseEntity<Void> saveUserPreference(@RequestBody UserPreference userPreference) {
        userPreferenceService.saveUserPreference(userPreference);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/UserPreference/getAll")
    public ResponseEntity<List<UserPreference>> getAllUserPreferences() {
        List<UserPreference> userPreferenceList = userPreferenceService.getAllUserPreferences();
        return ResponseEntity.ok(userPreferenceList);
    }

    @GetMapping("/UserPreference/{userId}")
    public ResponseEntity<UserPreference> getUserPreferenceByUserId(@PathVariable String userId) {
        UserPreference userPreference = userPreferenceService.getUserPreferenceByUserId(userId);
        if (userPreference == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(userPreference);
    }

    @PutMapping("/UserPreference/{userId}")
    public ResponseEntity<Void> updateUserPreference(@PathVariable String userId, @RequestBody UserPreference newUserPreference) {
        userPreferenceService.updateUserPreference(userId, newUserPreference);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/UserPreference/{userId}")
    public ResponseEntity<Void> deleteUserPreference(@PathVariable String userId) {
        userPreferenceService.deleteUserPreference(userId);
        return ResponseEntity.noContent().build();
    }
}
