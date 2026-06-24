package com.myTesi.aloisioUmberto.controller;

import com.myTesi.aloisioUmberto.config.JwtTokenProvider;
import com.myTesi.aloisioUmberto.data.services.interfaces.ImageService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://192.168.15.34:4200")
@Tag(name = "Image") //Name displayed on swagger

public class ImageController {

    private final ImageService imageService;

    @SecurityRequirement(name="Bearer Authentication")
    @GetMapping("/images/{userId}/{imagePath}")
    public ResponseEntity<Resource> getImageById(@PathVariable ("userId") String userId, @PathVariable("imagePath") String imagePath){
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(imageService.getImage(userId, imagePath));
    }

    //Non serve il token
    @GetMapping("/images/area/{areaId}")
    public ResponseEntity<Resource> getAreaImage(@PathVariable("areaId") String areaId) throws IOException {
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(imageService.getAreaImage(areaId));
    }



}
