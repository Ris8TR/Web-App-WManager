package com.myTesi.aloisioUmberto.controller;

import com.myTesi.aloisioUmberto.data.entities.InterestArea;
import com.myTesi.aloisioUmberto.data.services.interfaces.InterestAreaService;
import com.myTesi.aloisioUmberto.dto.New.NewForecastDto;
import com.myTesi.aloisioUmberto.dto.New.NewInterestAreaDto;
import com.myTesi.aloisioUmberto.dto.SensorDataDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
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
@Tag(name = "Forecast") //Name displayed on swagger

public class ForecastController {
        //private final InterestAreaService interestAreaService;

        @PostMapping(value = "/forecastData", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<NewForecastDto> addForecast(@RequestPart("data") NewForecastDto data, @RequestPart(value = "file" , required = false) MultipartFile file) throws IOException {
          return null;
        }


        @GetMapping("/forecast/{forecast}/latest-forecast-data/{token}")
        public ResponseEntity<List<SensorDataDto>> getLatestSensorDataForecast(@PathVariable String forecast, @PathVariable String token) {
            return null;
        }


    @GetMapping("/forecast/public/{forecast}/latest-forecast-data/")
    public ResponseEntity<List<SensorDataDto>> getLatestPublicSensorDataForecast(@PathVariable String forecast) {
        return null;
    }



    @GetMapping("/forecast/{id}/{token}")
        public ResponseEntity<InterestArea> getForecast(@PathVariable String id, @PathVariable String token) {
            return null;
        }


        @PutMapping("/forecast/update")
        public ResponseEntity<NewForecastDto> updateForecast(@RequestBody NewForecastDto forecastDto) {
            return null;
        }

        @DeleteMapping("/forecast/{id}")
        public ResponseEntity<Void> deleteForecast(@PathVariable ObjectId id) {
            return null;
        }

}
