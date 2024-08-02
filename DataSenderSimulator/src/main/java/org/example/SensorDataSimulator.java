package org.example;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SensorDataSimulator {

    private static final int NUM_SENSORS = 1000;
    private static final String URL = "http://localhost:8010/v1/SensorData";

    public static void main(String[] args) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(NUM_SENSORS);
        Random random = new Random();

        for (int i = 0; i < NUM_SENSORS; i++) {
            final int sensorId = i;
            scheduler.scheduleAtFixedRate(() -> sendSensorData(sensorId, random), 0, 10, TimeUnit.SECONDS);
        }
    }

    private static void sendSensorData(int sensorId, Random random) {
        try {
            String sensorDataJson = generateSensorDataJson(sensorId, random);
            CloseableHttpClient client = HttpClients.createDefault();
            System.out.println(sensorDataJson);
            HttpPost post = new HttpPost(URL);
            post.setHeader("Content-Type", "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW");

            HttpEntity entity = MultipartEntityBuilder.create()
                    .setBoundary("----WebKitFormBoundary7MA4YWxkTrZu0gW")
                    .addTextBody("data", sensorDataJson, ContentType.APPLICATION_JSON)
                    .build();

            post.setEntity(entity);

            try (CloseableHttpResponse response = client.execute(post)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                if (response.getStatusLine().getStatusCode() == 200) {
                    System.out.println("Data sent successfully for sensor " + sensorId);
                } else {
                    System.err.println("Failed to send data for sensor " + sensorId + ": " + response.getStatusLine().getStatusCode());
                    System.err.println("Response: " + responseBody);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String generateSensorDataJson(int sensorId, Random random) {
        String timestamp = Instant.now().toString();
        int CO2 = 350 + random.nextInt(100);
        double ap = 950 + (random.nextDouble() * 20);
        double temperature = 15 + (random.nextDouble() * 15);
        int humidity = 20 + random.nextInt(60);




        String apFormatted = String.format(Locale.US, "%.2f", ap);
        String temperatureFormatted = String.format(Locale.US, "%.2f", temperature);

        return String.format(
                """
                        {\
                        "sensorId": "%s",
                        "timestamp": "%s",
                        "payloadType": "json",
                        "latitude": %s,
                        "longitude": %s,
                        "payload": {
                            "CO2": %d,
                            "ap": %s,
                            "temperature":%s,
                            "humidity": %d
                          }\
                        }""",
                sensorId, timestamp, getRandomLatitude(random), getRandomLongitude(random), CO2, apFormatted, temperatureFormatted, humidity);
    }


    private static Double getRandomLatitude(Random random) {
        return roundToSixDecimalPlaces(-90 + (random.nextDouble() * 180));
    }

    private static Double getRandomLongitude(Random random) {
        return roundToSixDecimalPlaces(-180 + (random.nextDouble() * 360));
    }

    private static Double roundToSixDecimalPlaces(double value) {
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(6, BigDecimal.ROUND_HALF_UP);
        return bd.doubleValue();
    }

    private static Double roundToTwoDecimalPlaces(double value) {
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
        return bd.doubleValue();
    }

}
