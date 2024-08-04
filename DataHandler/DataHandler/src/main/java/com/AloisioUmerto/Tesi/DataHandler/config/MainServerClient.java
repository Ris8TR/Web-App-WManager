package com.AloisioUmerto.Tesi.DataHandler.config;
import com.AloisioUmerto.Tesi.DataHandler.data.entities.SensorData;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class MainServerClient {

    @Autowired
    private final RestTemplate restTemplate;

    @Value("${mainserver.url}")
    private String mainServerUrl;

    public ResponseEntity<String> sendSensorData(SensorData sensorData) {
        String url = mainServerUrl + "/v1/SensorData";
        return restTemplate.postForEntity(url, sensorData, String.class);
    }
}