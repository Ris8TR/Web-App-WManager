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
import org.data.dto.NewSensorDto;
import org.data.dto.NewUserDto;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

public class SensorService {

    private static final String NEW_SENSOR_URL = "http://192.168.15.34:8010/v1/sensors";
    private static final String SENSOR_DATA_URL = "http://localhost:8080/v1/SensorData";

    public void createSensorsForUser(String userId, NewUserDto newUser, String token, Random random, int numSensors) {
        List<NewSensorDto> sensors = new ArrayList<>();
        for (int j = 0; j < numSensors; j++) {
            NewSensorDto newSensor = createDummySensor(userId, newUser, j, random);
            String sensorId = sendNewSensor(newSensor, token);

            if (sensorId != null) {
                newSensor.setSensorId(sensorId);
                sensors.add(newSensor);
            } else {
                System.err.println("Failed to create sensor for user: " + newUser.getEmail());
            }
        }
        SensorDataSimulator.getUserSensorsMap().put(newUser.getEmail(), sensors);
    }

    public void sendSensorDataForUser(String LocaluserId, String userId, Random random) {
        try {
            String userEmail = "user" + LocaluserId + "@example.com";
            List<NewSensorDto> sensors = SensorDataSimulator.getUserSensorsMap().get(userEmail);

            if (sensors != null && !sensors.isEmpty()) {
                NewSensorDto sensor = sensors.get(random.nextInt(sensors.size()));
                String sensorDataJson = generateSensorDataJson(userId, sensor, "sensorPassword" + LocaluserId, random);

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
                        System.out.println("Data sent successfully for sensor " + sensor.getSensorId());
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

    private NewSensorDto createDummySensor(String userId, NewUserDto user, int sensorId, Random random) {
        NewSensorDto newSensor = new NewSensorDto();
        newSensor.setCompanyName("Company" + sensorId);
        newSensor.setPassword(user.getSensorPassword());
        newSensor.setDescription("Sensor description for sensor " + sensorId);
        newSensor.setUserId(userId);
        newSensor.setInterestAreaId(String.valueOf(1000 + random.nextInt(60)));
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
                      "interestAreaId": "%s"
                    }""",
                    newSensor.getCompanyName(), newSensor.getPassword(),
                    newSensor.getDescription(), token,
                    newSensor.getInterestAreaId()
            );

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

    private String generateSensorDataJson(String userId, NewSensorDto sensor, String userPass, Random random) {
        String timestamp = Instant.now().toString();
        int CO2 = 350 + random.nextInt(100);
        double ap = 950 + (random.nextDouble() * 20);
        double temperature = 15 + (random.nextDouble() * 15);
        int humidity = 20 + random.nextInt(60);

        String apFormatted = String.format(Locale.US, "%.2f", ap);
        String temperatureFormatted = String.format(Locale.US, "%.2f", temperature);

        return String.format(
                """
                {
                    "userId": "%s",
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
                userId, sensor.getSensorId(), timestamp, getRandomLatitude(random), getRandomLongitude(random),
                sensor.getInterestAreaId(), userPass, CO2, apFormatted, temperatureFormatted, humidity
        );
    }

    private Double getRandomLatitude(Random random) {
        return roundToSixDecimalPlaces(-90 + (random.nextDouble() * 180));
    }

    private Double getRandomLongitude(Random random) {
        return roundToSixDecimalPlaces(-180 + (random.nextDouble() * 360));
    }

    private Double roundToSixDecimalPlaces(double value) {
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(6, BigDecimal.ROUND_HALF_UP);
        return bd.doubleValue();
    }
}
