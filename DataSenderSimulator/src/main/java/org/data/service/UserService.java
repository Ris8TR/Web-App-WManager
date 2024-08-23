package org.data.service;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.data.dto.NewInterestAreaDto;
import org.data.dto.NewUserDto;
import org.json.JSONObject;

import java.util.Random;

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
    private static final String LOGIN_URL = "http://192.168.15.34:8010/v1/login/user";
    private static final String INTEREST_AREA_CREATION_URL = "http://192.168.15.34:8010/v1/interestarea";

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
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    userId = jsonResponse.getString("id");
                    System.out.println("User created successfully: " + newUser.getEmail());
                } else {
                    System.err.println("Failed to create user: " + response.getStatusLine().getStatusCode());
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
                    token = new JSONObject(responseBody).getString("token");
                    System.out.println("Login successful for user: " + newUser.getEmail());
                } else {
                    System.err.println("Failed to login: " + response.getStatusLine().getStatusCode());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return token;
    }

    public void createInterestAreasForUser(String userId, NewUserDto newUser, String token, Random random) {
        File directory = new File("Resource/shape");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File[] files = directory.listFiles();
        if (files == null || files.length == 0) {
            System.err.println("No shape files found in Resource/shape.");
            return;
        }

        for (int i = 0; i < 3; i++) {
            NewInterestAreaDto newArea = createDummyInterestArea(userId, i);
            File file = files[random.nextInt(files.length)];
            sendNewInterestArea(newArea, file, token);
        }
    }

    private NewInterestAreaDto createDummyInterestArea(String userId, int areaIndex) {
        NewInterestAreaDto newArea = new NewInterestAreaDto();
        newArea.setUserId(userId);
        newArea.setName("Interest Area " + areaIndex);
        newArea.setGeometry("Random Geometry " + areaIndex); // Replace with actual geometry if needed
        newArea.setType("Polygon");
        newArea.setDescription("Description for Interest Area " + areaIndex);
        return newArea;
    }

    private void sendNewInterestArea(NewInterestAreaDto newArea, File file, String token) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(INTEREST_AREA_CREATION_URL);
            post.setHeader("Authorization", "Bearer " + token);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addTextBody("data", new JSONObject(newArea).toString(), ContentType.APPLICATION_JSON);
            builder.addBinaryBody("file", file, ContentType.DEFAULT_BINARY, file.getName());

            post.setEntity(builder.build());

            try (CloseableHttpResponse response = client.execute(post)) {
                if (response.getStatusLine().getStatusCode() == 200) {
                    System.out.println("Interest area created successfully for user: " + newArea.getUserId());
                } else {
                    System.err.println("Failed to create interest area: " + response.getStatusLine().getStatusCode());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
