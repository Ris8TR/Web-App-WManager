package org.data;


import org.data.dto.InterestAreaDto;
import org.data.dto.NewSensorDto;
import org.data.dto.NewUserDto;
import org.data.service.SensorService;
import org.data.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SensorDataSimulator {

    private static final int NUM_SENSORS_PER_USER = 10;
    private static final int NUM_USERS = 5;
    private static final Map<String, List<NewSensorDto>> userSensorsMap = new HashMap<>();

    public static void main(String[] args) {
        Random random = new Random();
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(NUM_USERS);

        UserService userService = new UserService();
        SensorService sensorService = new SensorService();

        for (int i = 0; i < NUM_USERS; i++) {
            NewUserDto newUser = userService.createDummyUser(i, random);
            String userId = userService.sendNewUser(newUser);

            if (userId != null) {
                String token = userService.loginUser(newUser);
                if (token == null) {
                    System.err.println("Failed to login user: " + newUser.getEmail());
                    continue;
                }

                if (!token.isEmpty()) {
                    // Crea le aree di interesse per l'utente
                    List<InterestAreaDto> interestAreas = userService.createInterestAreasForUser(userId, token, random);
                    for (int j = 0; j < interestAreas.size(); j++) {
                        System.out.println( "Interest area: " + interestAreas.get(j).toString());
                    }

                    // Ora passa le aree di interesse come parametro
                    sensorService.createSensorsForUser(userId, newUser, token, random, NUM_SENSORS_PER_USER, interestAreas);
                }

                int finalI = i;
                scheduler.scheduleAtFixedRate(() -> sensorService.sendSensorDataForUser(String.valueOf(finalI), token, random), 0, 1, TimeUnit.SECONDS);
            } else {
                System.err.println("Failed to create user: " + newUser.getEmail());
            }
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
            }
        }));
    }


    public static Map<String, List<NewSensorDto>> getUserSensorsMap() {
        return userSensorsMap;
    }
}
