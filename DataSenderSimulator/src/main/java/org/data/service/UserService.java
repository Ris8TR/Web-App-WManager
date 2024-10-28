package org.data.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.data.dto.InterestAreaDto;
import org.data.dto.NewInterestAreaDto;
import org.data.dto.NewSensorDto;
import org.data.dto.NewUserDto;
import org.json.JSONObject;

import java.net.URL;
import java.time.Instant;
import java.util.*;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import java.io.File;
import java.util.Random;
public class UserService {

    private static final String USER_CREATION_URL = "http://192.168.15.34:8010/v1/users";
    private static final String LOGIN_URL = "http://192.168.15.34:8010/v1/auth/user";
    private static final String INTEREST_AREA_URL = "http://192.168.15.34:8010/v1/interestarea";

    public NewUserDto createDummyUser(int userId, Random random) {
        NewUserDto newUser = new NewUserDto();
        newUser.setEmail("user" + userId + "@example.com");
        newUser.setFirstName("FirstName" + userId);
        newUser.setLastName("LastName" + userId);
        newUser.setPassword("password" + userId);
        newUser.setSensorPassword("sensorPassword" + userId);
        System.out.println(newUser.toString());
        return newUser;
    }

    public String sendNewUser(NewUserDto newUser) {
        String userId = null;
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(USER_CREATION_URL);
            post.setHeader("Content-Type", "application/json");
            post.setHeader("Accept", "application/json");

            String userJson = String.format(
                    """
                    {
                      "email": "%s",
                      "firstName": "%s",
                      "lastName": "%s",
                      "password": "%s",
                      "sensorPassword": "%s"
                    }""",
                    newUser.getEmail(), newUser.getFirstName(), newUser.getLastName(),
                    newUser.getPassword(), newUser.getSensorPassword()
            );

            StringEntity entity = new StringEntity(userJson, ContentType.APPLICATION_JSON);
            post.setEntity(entity);

            try (CloseableHttpResponse response = client.execute(post)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                if (response.getStatusLine().getStatusCode() == 200) {
                    System.out.println("User created successfully: " + newUser.getEmail());
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    userId = jsonResponse.getString("id");
                } else if (response.getStatusLine().getStatusCode() == 409) {

                    userId = loginUser(newUser);;
                } else {
                    System.err.println("Failed to create user: " + response.getStatusLine().getStatusCode());
                    System.err.println("Response: " + responseBody);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userId;
    }

    public String loginUser(NewUserDto newUser) {
        String token = null;
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(LOGIN_URL);
            post.setHeader("Content-Type", "application/json");
            post.setHeader("Accept", "application/json");

            String loginJson = String.format(
                    """
                    {
                      "email": "%s",
                      "password": "%s"
                    }""",
                    newUser.getEmail(), newUser.getPassword()
            );

            StringEntity entity = new StringEntity(loginJson, ContentType.APPLICATION_JSON);
            post.setEntity(entity);

            try (CloseableHttpResponse response = client.execute(post)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                if (response.getStatusLine().getStatusCode() == 200) {
                    token = parseTokenFromResponse(responseBody);
                    System.out.println("Login successful for user: " + newUser.getEmail());
                } else {
                    System.err.println("Failed to login: " + response.getStatusLine().getStatusCode());
                    System.err.println("Response: " + responseBody);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return token;
    }

    public String loginExistingUser(NewUserDto newUser) {
        String token = null;
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(LOGIN_URL);
            post.setHeader("Content-Type", "application/json");
            post.setHeader("Accept", "application/json");

            String loginJson = String.format(
                    """
                    {
                      "email": "%s",
                      "password": "%s"
                    }""",
                    newUser.getEmail(), newUser.getPassword()
            );

            StringEntity entity = new StringEntity(loginJson, ContentType.APPLICATION_JSON);
            post.setEntity(entity);

            try (CloseableHttpResponse response = client.execute(post)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                if (response.getStatusLine().getStatusCode() == 200) {
                    System.out.println("Login successful for user: " + newUser.getEmail());
                    new JSONObject(responseBody).getString("id");
                } else {
                    System.err.println("Failed to login: " + response.getStatusLine().getStatusCode());
                    System.err.println("Response: " + responseBody);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String parseTokenFromResponse(String responseBody) {
        return new JSONObject(responseBody).getString("token");
    }

    public List<InterestAreaDto> createInterestAreasForUser(String userId, String token, Random random) {
        List<InterestAreaDto> interestAreas = new ArrayList<>();

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            for (int i = 0; i < 1; i++) {
                NewInterestAreaDto interestAreaDto = new NewInterestAreaDto();
                interestAreaDto.setUserId(userId);
                interestAreaDto.setName("Interest Area " + i);
                interestAreaDto.setDescription("Description for interest area " + i);
                interestAreaDto.setToken(token);

                // Costruisci l'entity multipart
                MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();

                // Aggiungi i dati JSON
                entityBuilder.addTextBody("data", new ObjectMapper().writeValueAsString(interestAreaDto), ContentType.APPLICATION_JSON);

                // Verifica se ci sono file disponibili prima di selezionarne uno
                ClassLoader classLoader = getClass().getClassLoader();
                URL resourceUrl = classLoader.getResource("shape");

                if (resourceUrl != null) {
                    File directory = new File(resourceUrl.getFile());
                    File[] shapeFiles = directory.listFiles();
                    if (shapeFiles != null && shapeFiles.length > 0) {
                        // Prendi un file casuale dalla directory
                        File file = shapeFiles[random.nextInt(shapeFiles.length)];
                        // Aggiungi il file con il nome corretto e il tipo di contenuto specificato
                        entityBuilder.addPart("file", new FileBody(file));
                    } else {
                        System.err.println("No files found in resources/shape.");
                    }
                } else {
                    System.err.println("Directory 'shape' not found in resources.");
                }

                HttpEntity entity = entityBuilder.build();

                // Crea la richiesta POST
                HttpPost post = new HttpPost(INTEREST_AREA_URL);
                post.setEntity(entity);

                // Esegui la richiesta e gestisci la risposta
                try (CloseableHttpResponse response = client.execute(post)) {
                    String responseBody = EntityUtils.toString(response.getEntity());

                    if (response.getStatusLine().getStatusCode() == 200) {
                        InterestAreaDto createdInterestArea = new ObjectMapper().readValue(responseBody, InterestAreaDto.class);
                        interestAreas.add(createdInterestArea);
                        System.out.println(createdInterestArea.getGeometry());
                        System.out.println("Interest area created successfully: " + createdInterestArea.getName());
                    } else {
                        System.err.println("Failed to create interest area: " + response.getStatusLine().getStatusCode());
                        System.err.println("Response: " + responseBody);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return interestAreas;
    }






    private String generateSensorDataJson( NewInterestAreaDto newInterestAreaDto, String token) {


        return String.format(
                """
                {
                    "userId": "%s",
                    "name": "%s",
                    "description": "%s",
                    "token": "%s"
                }""",
                newInterestAreaDto.getUserId(), newInterestAreaDto.getName(), newInterestAreaDto.getDescription(), token
        );
    }
}