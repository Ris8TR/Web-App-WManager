package org.data.service;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.data.SensorDataSimulator;
import org.data.dto.InterestAreaDto;
import org.data.dto.NewSensorDto;
import org.data.dto.NewUserDto;
import org.json.JSONObject;

import java.time.Instant;
import java.util.*;

public class SensorService {

    private static final String NEW_SENSOR_URL = "http://192.168.15.34:8010/v1/sensors";
    private static final String SENSOR_DATA_URL = "http://localhost:8010/v1/SaveSensorData";
    Map<String, InterestAreaDto> interestAreasForSensor = new HashMap<>();

    public void createSensorsForUser(String userId, NewUserDto newUser, String token, Random random, int numSensors, List<InterestAreaDto> interestAreas) {
        List<NewSensorDto> sensors = new ArrayList<>();

        for (int j = 0; j < numSensors; j++) {
            // Prendi un'area di interesse casuale
            InterestAreaDto interestArea = interestAreas.get(random.nextInt(interestAreas.size()));
            NewSensorDto newSensor = createDummySensor(userId, newUser, j, random, interestArea.getId());
            String sensorId = sendNewSensor(newSensor, token);

            if (sensorId != null) {
                newSensor.setSensorId(sensorId);
                sensors.add(newSensor);
                interestAreasForSensor.put(sensorId, interestArea);
            } else {
                System.err.println("Failed to create sensor for user: " + newUser.getEmail());
            }
        }
        SensorDataSimulator.getUserSensorsMap().put(newUser.getEmail(), sensors);
    }

    private NewSensorDto createDummySensor(String userId, NewUserDto user, int sensorId, Random random, String interestAreaId) {
        NewSensorDto newSensor = new NewSensorDto();
        newSensor.setCompanyName("Company" + sensorId);
        newSensor.setPassword(user.getSensorPassword());
        newSensor.setDescription("Sensor description for sensor " + sensorId);
        newSensor.setUserId(userId);
        newSensor.setInterestAreaId(interestAreaId);
        return newSensor;
    }

    private String sendNewSensor(NewSensorDto newSensor, String token) {
        String sensorId = null;
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(NEW_SENSOR_URL);
            post.setHeader("Content-Type", "application/json");
            post.setHeader("Accept", "application/json");

            String sensorJson = String.format(
                    """
                    {
                      "companyName": "%s",
                      "password": "%s",
                      "description": "%s",
                      "userId": "%s",
                      "token": "%s",
                      "interestAreaId": "%s"
                    }""",
                    newSensor.getCompanyName(), newSensor.getPassword(),
                    newSensor.getDescription(), token,token,
                    newSensor.getInterestAreaId()
            );

            System.out.println(sensorJson);

            StringEntity entity = new StringEntity(sensorJson, ContentType.APPLICATION_JSON);
            post.setEntity(entity);

            try (CloseableHttpResponse response = client.execute(post)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                if (response.getStatusLine().getStatusCode() == 200) {
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    sensorId = jsonResponse.getString("id");
                    newSensor.setSensorId(sensorId);

                    System.out.println("Sensor created successfully: " + newSensor);

                } else {
                    System.err.println("Failed to create sensor: " + response.getStatusLine().getStatusCode());
                    System.err.println("Response: " + responseBody);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sensorId;
    }

    public void sendSensorDataForUser(String localUserId, String token, Random random) {
        try {
            String userEmail = "user" + localUserId + "@example.com";
            List<NewSensorDto> sensors = SensorDataSimulator.getUserSensorsMap().get(userEmail);

            if (sensors != null && !sensors.isEmpty()) {
                NewSensorDto sensor = sensors.get(random.nextInt(sensors.size()));

                // Simula la latitudine e longitudine dall'area di interesse associata al sensore
                InterestAreaDto interestArea = interestAreasForSensor.get(sensor.getSensorId());
                String sensorDataJson = generateSensorDataJson(token, sensor, "sensorPassword" + localUserId, random, interestArea.getGeometry(), interestArea.getId());

                CloseableHttpClient client = HttpClients.createDefault();
                HttpPost post = new HttpPost(SENSOR_DATA_URL);
                post.setHeader("Content-Type", "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW");

                HttpEntity entity = MultipartEntityBuilder.create()
                        .setBoundary("----WebKitFormBoundary7MA4YWxkTrZu0gW")
                        .addTextBody("data", sensorDataJson, ContentType.APPLICATION_JSON)
                        .build();

                post.setEntity(entity);

                try (CloseableHttpResponse response = client.execute(post)) {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    if (response.getStatusLine().getStatusCode() == 200) {
                        //System.out.println("Data sent successfully for sensor " + sensor.getSensorId());
                        System.out.println(sensorDataJson);
                    } else {
                        System.err.println("Failed to send data for sensor " + sensor.getSensorId() + ": " + response.getStatusLine().getStatusCode());
                        System.err.println("Response: " + responseBody);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String generateSensorDataJson(String token, NewSensorDto sensor, String userPass, Random random, String geometry, String interestAreaId) {
        String timestamp = Instant.now().toString();
        int CO2 = 350 + random.nextInt(100);
        double ap = 950 + (random.nextDouble() * 20);
        double temperature = 15 + (random.nextDouble() * 15);
        int humidity = 20 + random.nextInt(60);


        String apFormatted = String.format(Locale.US, "%.2f", ap);
        String temperatureFormatted = String.format(Locale.US, "%.2f", temperature);

        // Estrai una latitudine e longitudine dalla geometria dell'area di interesse
        double latitude = getRandomLatitudeFromGeometry(geometry, random);
        double longitude = getRandomLongitudeFromGeometry(geometry, random);


        return String.format(
                """
                {
                    "token": "%s",
                    "sensorId": "%s",
                    "timestamp": "%s",
                    "payloadType": "json",
                    "latitude": %s,
                    "longitude": %s,
                    "interestAreaId": "%s",
                    "sensorPassword": "%s",
                    "payload": {
                        "CO2": %d,
                        "ap": %s,
                        "temperature": %s,
                        "humidity": %d
                    }
                }""",
                token, sensor.getSensorId(), timestamp, latitude, longitude,
                interestAreaId, userPass, CO2, apFormatted, temperatureFormatted, humidity
        );
    }

    private double getRandomLatitudeFromGeometry(String geometry, Random random) {
        return extractCoordinateFromGeometry(geometry, random, true);
    }

    private double getRandomLongitudeFromGeometry(String geometry, Random random) {
        return extractCoordinateFromGeometry(geometry, random, false);
    }

    private double extractCoordinateFromGeometry(String geometry, Random random, boolean isLatitude) {
        String[] parts = geometry.replace("MULTILINESTRING", "")
                .replace("(", "")
                .replace(")", "")
                .split(",");

        if (parts.length < 2) {
            parts = new String[] { parts[0], parts[0] };
        }

        // Seleziona una coordinata casuale
        String[] coordinates = parts[random.nextInt(parts.length)].trim().split(" ");

        // Rimuovi eventuali caratteri non numerici e converti in double
        String coordinateString = isLatitude ? coordinates[1] : coordinates[0];
        coordinateString = coordinateString.replaceAll("[^0.1-9\\-]", "");

        // Aggiungi una piccola variazione alla coordinata
        double coordinate = Double.parseDouble(coordinateString);
        double variation = (random.nextDouble() - 0.5) * 0.00050;
        return coordinate + variation;
    }

}
